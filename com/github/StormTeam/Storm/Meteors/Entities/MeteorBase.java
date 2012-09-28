package com.github.StormTeam.Storm.Meteors.Entities;

import com.github.StormTeam.Storm.Storm;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.Entity;

import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MathHelper;
import net.minecraft.server.MovingObjectPosition;
import net.minecraft.server.Vec3D;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ProjectileHitEvent;

public class MeteorBase extends EntityFireball {

    protected float explosionRadius = 50F;
    protected float trailPower = 20F;
    protected float brightness = 10F;
    protected String meteorCrashMessage;
    protected int burrowCount = 5;
    protected int burrowPower = 10;
    protected boolean spawnMeteorOnImpact;
    protected int radius;
    protected String damageMessage;
    protected int shockwaveDamage;
    protected int shockwaveDamageRadius;
    protected int snowRadius;
    protected boolean h_lock, h_lock_2, h_lock_3;
    protected Field e, u, h, j, an, i;

    public MeteorBase(World world) {
        super(world);
        try {
            if (Storm.version == 1.3) {
                e = EntityFireball.class.getDeclaredField("e");
                i = EntityFireball.class.getDeclaredField("i");
                h = EntityFireball.class.getDeclaredField("h");
                j = EntityFireball.class.getDeclaredField("j");
                an = EntityFireball.class.getDeclaredField("an");

                i.setAccessible(true);
                h.setAccessible(true);
                j.setAccessible(true);
                an.setAccessible(true);
                e.setAccessible(true);

            }

            if (Storm.version == 1.2) {
                //TODO DO IT!
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MeteorBase(World world, int burrowCount, int burrowPower,
            float trailPower, float explosionRadius, float brightness,
            String crashMessage, int shockwaveDamage,
            int shockwaveDamageRadius, String damageMessage, int snowRadius,
            boolean spawnOnImpact, int radius) {

        super(world);

        // Massive objects require massive initializations...
        this.burrowPower = burrowPower;
        this.burrowCount = burrowCount;
        this.trailPower = trailPower;
        this.explosionRadius = explosionRadius;
        this.brightness = brightness;
        this.meteorCrashMessage = crashMessage;
        this.shockwaveDamage = shockwaveDamage;
        this.shockwaveDamageRadius = shockwaveDamageRadius;
        this.damageMessage = damageMessage;
        this.snowRadius = snowRadius;
        this.damageMessage = damageMessage;
        this.spawnMeteorOnImpact = spawnOnImpact;
        this.radius = radius;

    }

    public void spawn() {
        world.addEntity(this, SpawnReason.CUSTOM);

    }

    public boolean move() {

        do {
            h_lock = !h_lock;
            if (h_lock) {
                break;
            }
            h_lock_2 = !h_lock_2;
            if (h_lock_2) {
                break;
            }
            h_lock_3 = !h_lock_3;
            if (h_lock_3) {
                break;
            }

            int locY = (int) (this.locY);
            if ((locY & 0xFFFFFF00) != 0) { // !(0x00 < locY < 0xFF)
                this.dead = true; // Die silently
                return false;
            }

            if ((locY & 0xFFFFFFE0) == 0) { // locy < 32
                explode();
                return false;
            }

            world.createExplosion(this, locX, locY, locZ, trailPower, true);
            return false;
        } while (false);
        motX *= 0.909F;
        motY *= 0.909F;
        motZ *= 0.909F;
        return true;


    }

    public void burrow() {

        if (burrowCount > 0) {
            // Not yet dead, so burrow.
            world.createExplosion(this, locX, locY, locZ, burrowPower, true);
            --burrowCount;
            return;
        }
        makeWinter();
        explode();

    }

    public void explode() {
        world.createExplosion(this, locX, locY, locZ, explosionRadius, true);

        Storm.util.damageNearbyPlayers(new Location(this.world.getWorld(),
                locX, locY, locZ), shockwaveDamageRadius, shockwaveDamage,
                damageMessage);

        for (Player p : world.getWorld().getPlayers()) {
            Storm.util.message(
                    p,
                    this.meteorCrashMessage.replace("%x", (int) locX + "")
                    .replace("%z", (int) locZ + "")
                    .replace("%y", (int) locY + ""));
        }
        if (this.spawnMeteorOnImpact) {
            this.spawnMeteor(new Location(world.getWorld(), locX, locY, locZ));
        }
        die();
    }

    private void spawnMeteor(Location expl) {
        ArrayList<Material> m = new ArrayList<Material>();
        m.add(Material.COAL_ORE);
        m.add(Material.IRON_ORE);
        m.add(Material.REDSTONE_ORE);
        m.add(Material.GOLD_ORE);
        m.add(Material.EMERALD_ORE);
        m.add(Material.DIAMOND_ORE);
        m.add(Material.LAPIS_ORE);
        while (expl.getBlock().getType().equals(Material.AIR)) {
            expl.add(0, -1, 0);
        }
        expl.add(0, radius + 1, 0);
        this.makeSphere(expl, null, radius, true, true, m);
        this.makeSphere(expl, Material.OBSIDIAN, radius, false, false, null);
    }

    public void makeSphere(Location pos, Material block, double radius,
            boolean filled, boolean random, ArrayList<Material> m) {
        double radiusX = radius + 0.5;
        double radiusY = radius + 0.5;
        double radiusZ = radius + 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);
        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (!filled) {
                        if (lengthSq(nextXn, yn, zn) <= 1
                                && lengthSq(xn, nextYn, zn) <= 1
                                && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    if (!random) {
                        pos.clone().add(x, y, z).getBlock().setType(block);
                        pos.clone().add(-x, y, z).getBlock().setType(block);
                        pos.clone().add(x, y, -z).getBlock().setType(block);
                        pos.clone().add(-x, -y, z).getBlock().setType(block);
                        pos.clone().add(x, -y, -z).getBlock().setType(block);
                        pos.clone().add(-x, y, -z).getBlock().setType(block);
                        pos.clone().add(-x, -y, -z).getBlock().setType(block);
                        pos.clone().add(x, -y, z).getBlock().setType(block);
                    } else {
                        pos.clone().add(x, y, z).getBlock()
                                .setType(chooseRandom(m));
                        pos.clone().add(-x, y, z).getBlock()
                                .setType(chooseRandom(m));
                        pos.clone().add(x, y, -z).getBlock()
                                .setType(chooseRandom(m));
                        pos.clone().add(-x, -y, z).getBlock()
                                .setType(chooseRandom(m));
                        pos.clone().add(x, -y, -z).getBlock()
                                .setType(chooseRandom(m));
                        pos.clone().add(-x, y, -z).getBlock()
                                .setType(chooseRandom(m));
                        pos.clone().add(-x, -y, -z).getBlock()
                                .setType(chooseRandom(m));
                        pos.clone().add(x, -y, z).getBlock()
                                .setType(chooseRandom(m));
                    }
                }
            }
        }

    }

    private Material chooseRandom(ArrayList<Material> m) {
        return m.get(new Random().nextInt(m.size()));
    }

    private static final double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    public void makeWinter() {
        CraftWorld craftworld = world.getWorld();
        int radiusSquared = snowRadius * snowRadius;

        for (int x = -snowRadius; x <= snowRadius; x++) {
            for (int z = -snowRadius; z <= snowRadius; z++) {
                if ((x * x) + (z * z) <= radiusSquared) {
                    craftworld.getHighestBlockAt((int) (x + locX),
                            (int) (z + locZ)).setBiome(Biome.TAIGA);
                }
            }
        }
    }
    
    public void updatePosition() {
        
            if ((!this.world.isStatic) && (((this.shooter != null) && (this.shooter.dead)) || (!this.world.isLoaded((int)this.locX, (int)this.locY, (int)this.locZ)))) {
      die();
    } else {
      //super.F_();
      setOnFire(1);
//      if (this.i) {
//        int i = this.world.getTypeId(this.e, this.f, this.g);
//
//        if (i == this.h) {
//          this.j += 1;
//          if (this.j == 600) {
//            die();
//          }
//
//          return;
//        }
//
//        this.i = false;
//        this.motX *= this.random.nextFloat() * 0.2F;
//        this.motY *= this.random.nextFloat() * 0.2F;
//        this.motZ *= this.random.nextFloat() * 0.2F;
//        this.j = 0;
//        this.k = 0; //an in newer
//      } else {
//        this.k += 1;
//      }

      Vec3D vec3d = Vec3D.a(this.locX, this.locY, this.locZ);
      Vec3D vec3d1 = Vec3D.a(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
      MovingObjectPosition movingobjectposition = this.world.a(vec3d, vec3d1);

      vec3d = Vec3D.a(this.locX, this.locY, this.locZ);
      vec3d1 = Vec3D.a(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
      if (movingobjectposition != null) {
        vec3d1 = Vec3D.a(movingobjectposition.pos.a, movingobjectposition.pos.b, movingobjectposition.pos.c);
      }

      Entity entity = null;
      List list = this.world.getEntities(this, this.boundingBox.a(this.motX, this.motY, this.motZ).grow(1.0D, 1.0D, 1.0D));
      double d0 = 0.0D;

      for (int j = 0; j < list.size(); j++) {
        Entity entity1 = (Entity)list.get(j);


      }

      if (entity != null) {
        movingobjectposition = new MovingObjectPosition(entity);
      }

      if (movingobjectposition != null) {
        a(movingobjectposition);

        if (this.dead) {
          ProjectileHitEvent phe = new ProjectileHitEvent((Projectile)getBukkitEntity());
          this.world.getServer().getPluginManager().callEvent(phe);
        }

      }

      this.locX += this.motX;
      this.locY += this.motY;
      this.locZ += this.motZ;
      float f1 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);

      this.yaw = (float)(Math.atan2(this.motX, this.motZ) * 180.0D / 3.141592741012573D);

      for (this.pitch = (float)(Math.atan2(this.motY, f1) * 180.0D / 3.141592741012573D); this.pitch - this.lastPitch < -180.0F; this.lastPitch -= 360.0F);
      while (this.pitch - this.lastPitch >= 180.0F) {
        this.lastPitch += 360.0F;
      }

      while (this.yaw - this.lastYaw < -180.0F) {
        this.lastYaw -= 360.0F;
      }

      while (this.yaw - this.lastYaw >= 180.0F) {
        this.lastYaw += 360.0F;
      }

      this.pitch = (this.lastPitch + (this.pitch - this.lastPitch) * 0.2F);
      this.yaw = (this.lastYaw + (this.yaw - this.lastYaw) * 0.2F);
      float f2 = 0.95F;

      this.motX += this.dirX;
      this.motY += this.dirY;
      this.motZ += this.dirZ;
      this.motX *= f2;
      this.motY *= f2;
      this.motZ *= f2;
      this.world.a("smoke", this.locX, this.locY + 0.5D, this.locZ, 0.0D, 0.0D, 0.0D);
      setPosition(this.locX, this.locY, this.locZ);
    }
        
    }
    
    public EntityLiving shooter;
    public double dirX;
    public double dirY;
    public double dirZ;
    public int yield = 0;
    public boolean isIncendiary;
}