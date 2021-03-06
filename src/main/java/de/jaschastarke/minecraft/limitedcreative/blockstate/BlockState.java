package de.jaschastarke.minecraft.limitedcreative.blockstate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotNull;

@Entity
@Table(name = "block_state")
@IdClass(BlockLocation.class)
public class BlockState {
    public static enum Source {
        SEED, // There is no way to determine this source, but lets be prepared for miracles ;)
        PLAYER,
        EDIT, // WorldEdit or MCEdit or such, we also can't determine that. But I keep believing in miracles
        UNKNOWN
    }

    /*@Id
    private UUID world;
    @Id
    private int x;
    @Id
    private int y;
    @Id
    private int z;*/
    
    @EmbeddedId
    private BlockLocation blockLocation;
    
    @Column(name = "gm")
    private GameMode gameMode;
    
    @Column(name = "player")
    private String playerName;
    
    @NotNull
    @Column(name = "cdate")
    private Date date;
    
    @NotNull
    private Source source = Source.UNKNOWN;


    public BlockLocation getBlockLocation() {
        return blockLocation;
    }

    public void setBlockLocation(BlockLocation loc) {
        this.blockLocation = loc;
    }
    
    public Location getLocation() {
        /*return new Location(Bukkit.getWorld(world), x, y, z);*/
        return getBlockLocation().getLocation();
    }

    public void setLocation(Location loc) {
        /*world = loc.getWorld().getUID();
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();*/
        setBlockLocation(new BlockLocation(loc));
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gm) {
        this.gameMode = gm;
    }

    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String s) {
        playerName = s;
    }
    
    public OfflinePlayer getPlayer() {
        OfflinePlayer p = Bukkit.getPlayerExact(playerName);
        if (p == null)
            p = Bukkit.getOfflinePlayer(playerName);
        return p;
    }

    public void setPlayer(OfflinePlayer player) {
        setSource(Source.PLAYER);
        this.playerName = player.getName();
        if (player instanceof Player) {
            setGameMode(((Player) player).getGameMode());
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        if (source != Source.PLAYER)
            setPlayer(null);
        this.source = source;
    }

    @Override
    public String toString() {
        return blockLocation.toString() + " by " +
            (source == Source.PLAYER ? playerName : (source.toString() + (playerName != null ? "(" + playerName + ")" : ""))) +
            (gameMode != null ? "" : (" in GM: " + gameMode)) + 
            " at " + date.toString();
    }
}
