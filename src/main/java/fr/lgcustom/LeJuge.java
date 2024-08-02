package fr.lgcustom;

import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.*;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleNeutral;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Role(key = LGCustom.KEY+".role.lejuge.display", category = Category.NEUTRAL, attribute = RoleAttribute.NEUTRAL, defaultAura = Aura.NEUTRAL)
public class LeJuge extends RoleNeutral {
    private double bonusForce = 0, bonusResi =0;
    private KillRunnable killRunnable;
    public LeJuge(WereWolfAPI game, IPlayerWW playerWW) {
        super(game, playerWW);
        Bukkit.getScheduler().runTaskLaterAsynchronously(LGCustom.getInstance(), () -> {
            this.killRunnable = new KillRunnable(this);
            killRunnable.runTaskTimerAsynchronously(LGCustom.getInstance(), 0, 20);
            Bukkit.broadcastMessage("Le runnable du juge a démarré");
            killRunnable.chooseTarget();
        }, 100);
    }

    @Override
    public @NotNull String getDescription() {
        return new DescriptionBuilder(this.game, this).setDescription(this.game.translate("nicknqck.role.lejuge.description"))
                .build();
    }

    @Override
    public void recoverPower() {
    }

    @Override
    public void recoverPotionEffect() {
        this.getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1, "Force"));
    }
    @EventHandler(priority = EventPriority.NORMAL)
    private void onKill(FinalDeathEvent event) {
        if (event.isCancelled())return;
        if (this.killRunnable == null)return;
        if (!event.getPlayerWW().getLastKiller().isPresent())return;
        if (killRunnable.uuidTarget == null)return;
        if (killRunnable.killTarget)return;
        IPlayerWW killer = event.getPlayerWW().getLastKiller().get();
            if (killer.getRole().isAbilityEnabled()) {
                if (killer.getRole() instanceof LeJuge) {
                    if (killer.getRole().getPlayerUUID().equals(this.getPlayerUUID())) {
                        if (event.getPlayerWW().getRole().getPlayerUUID().equals(killRunnable.uuidTarget)) {
                            killRunnable.onKillTarget(killer);
                        }
                    }
                }
        }
    }
    @EventHandler
    private void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().getUniqueId().equals(this.getPlayerUUID())) {
            event.setDamage(event.getDamage()*(1+(this.bonusForce/100)));
        } else if (event.getEntity().getUniqueId().equals(this.getPlayerUUID())) {
            double reductionFactor = 1 - (this.bonusResi / 100);
            event.setDamage(event.getDamage() * reductionFactor);
        }
    }
    @EventHandler
    private void onWin(WinEvent event) {
        if (this.killRunnable != null) {
            Player player = Bukkit.getPlayer(this.getPlayerUUID());
            if (player != null) {
                killRunnable.addSpeedAtInt(player, -killRunnable.speedAdded);
            }
            this.killRunnable.cancel();
        }
    }

    private static class KillRunnable extends BukkitRunnable {
        private final LeJuge leJuge;
        private int actualTimer;
        private UUID uuidTarget;
        private int speedAdded = 0;
        private boolean killTarget = false;
        private KillRunnable(LeJuge juge) {
            this.leJuge = juge;
            this.actualTimer = 60*10;
        }
        @Override
        public void run() {
            if (leJuge.getPlayerWW().isState(StatePlayer.ALIVE)) {
                actualTimer--;
                if (actualTimer == 0) {
                    if (!killTarget && uuidTarget != null) {
                        leJuge.getPlayerWW().removePlayerMaxHealth(1.0);
                        Player player = Bukkit.getPlayer(leJuge.getPlayerUUID());
                        if (player != null) {
                            player.sendMessage("§7Vous avez perdu de la§c vie§7 suite à l'échec de votre mission.");
                        }
                        chooseTarget();
                        Bukkit.broadcastMessage("Le juge a perdu 1 coeurs permanent");
                    }
                }
            }
        }
        private void chooseTarget(){
            Bukkit.broadcastMessage("Choosing target for LeJuge.java");
            final List<IPlayerWW> igPlayers = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (LGCustom.getInstance().getApi().getPlayerWW(player.getUniqueId()).isPresent()) {
                    IPlayerWW iPlayer = LGCustom.getInstance().getApi().getPlayerWW(player.getUniqueId()).get();
                    igPlayers.add(iPlayer);
                }
            }
            final List<IPlayerWW> goodPlayers = new ArrayList<>();
            for (final IPlayerWW p : igPlayers) {
                if (p.getRole() != null) {
                    IRole role = p.getRole();
                    if (!role.getCamp().equals(Camp.NEUTRAL)) {
                        goodPlayers.add(p);
                    }
                }
            }
            if (goodPlayers.isEmpty()) {
                Player owner = Bukkit.getPlayer(leJuge.getPlayerUUID());
                if (owner != null) {
                    owner.sendMessage("§7Aucune§c cible§7 n'a été trouver.");
                }
                return;
            }
            while (this.uuidTarget == null) {
                Collections.shuffle(goodPlayers, LGCustom.RANDOM);
                if (LGCustom.RANDOM.nextInt(100) <= 10 && goodPlayers.get(0) != null && goodPlayers.get(0).getMojangUUID().isPresent()) {
                    this.uuidTarget = goodPlayers.get(0).getMojangUUID().get();
                }
            }

            leJuge.getPlayerWW().sendMessage(new TextComponent("§7Votre §ccible§7 est maintenant: "+Bukkit.getPlayer(uuidTarget).getName()));
        }
        private void onKillTarget(IPlayerWW killer) {
            Player victim = Bukkit.getPlayer(uuidTarget);
            killTarget = true;
            uuidTarget = null;
            //1 speed 2 force 3 resi
            if (victim == null)return;
            int random = LGCustom.RANDOM.nextInt(4);
            StringBuilder sb = new StringBuilder();
            sb.append("§7Vous avez réussi à tuer votre§c cible§7, vous obtenez donc§c +5%§7 de ");
            if (random == 0) {
                speedAdded+=5;
                addSpeedAtInt(victim, 5);
                sb.append("§cSpeed");
            } else if (random == 1) {
                leJuge.bonusForce +=5;
                sb.append("§cForce");
            } else if (random == 2) {
                leJuge.bonusResi +=5;
                sb.append("§cRésistance");
            }
            sb.append("§7.");
            killer.sendMessage(new TextComponent(sb.toString()));
            Bukkit.broadcastMessage("Une cible a été chercher pour le juge");
        }
        public void addSpeedAtInt(Player player, float speedpercent) {player.setWalkSpeed(player.getWalkSpeed()+(speedpercent/500));}
    }
}