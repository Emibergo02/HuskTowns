package dev.unnm3d.kalyaclaims;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.*;

public class RetainTask extends BukkitRunnable {

    private final BukkitHuskTowns huskTowns;
    private final HashMap<Town, TreeMap<Float, Claim>> townClaimPrices;
    private Calendar scheduleTime;
    private boolean alreadyRun;
    private static final int CLAIM_PRICE = 100;
    private static final int PLOT_PRICE = 100;
    private static final int FARM_PRICE = 100;
    private static final int COLONY_PRICE = 100;


    public RetainTask(BukkitHuskTowns huskTowns) {
        this.huskTowns = huskTowns;
        this.townClaimPrices = new HashMap<>();
        this.scheduleTime = schedule();

        final long secondsTillRun = ((scheduleTime.getTimeInMillis() - System.currentTimeMillis()) / 1000);

        //If the task should run in less than 10 minutes, run it 10 seconds after the task is registered
        this.runTaskTimer(huskTowns, (secondsTillRun < 600 ? 10 : secondsTillRun) * 20, secondsTillRun * 20);

    }

    private Calendar schedule() {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone(huskTowns.getSettings().getRetainTimeZone()));
        Calendar taskTime = (Calendar) currentTime.clone();

        // Craft a calendar object for the time of day the task should run
        String[] splitTime = huskTowns.getSettings().getRetainTaskTime().split(":");
        taskTime.set(Calendar.HOUR_OF_DAY, splitTime.length > 0 ? Integer.parseInt(splitTime[0]) : 0);
        taskTime.set(Calendar.MINUTE, splitTime.length > 1 ? Integer.parseInt(splitTime[1]) : 0);
        taskTime.set(Calendar.SECOND, splitTime.length > 2 ? Integer.parseInt(splitTime[2]) : 0);

        // If the time is before the current time, add a day
        if (taskTime.before(currentTime)) taskTime.add(Calendar.DAY_OF_MONTH, 1);

        this.alreadyRun = false;
        return taskTime;
    }

    @Override
    public void run() {
        townClaimPrices.clear();
        huskTowns.getTowns().forEach(town -> {
            TreeMap<Float, Claim> townRetainPrices = townRetainPrices(town);
            if (!townRetainPrices.isEmpty()) {
                townClaimPrices.put(town, townRetainPrices);
                Map<Float, Claim> unpaidClaims = payClaims(town, townRetainPrices);

            }
        });
    }

    /**
     * Pay claims of a town
     * Returns a map of claims that could not be paid
     *
     * @param town             Town to pay claims for
     * @param townRetainPrices TreeMap of claims and their prices
     * @return Map of claims that could not be paid
     */
    private Map<Float, Claim> payClaims(Town town, TreeMap<Float, Claim> townRetainPrices) {
        BigDecimal townBalance = town.getMoney();
        for (Map.Entry<Float, Claim> priceClaimEntry : townRetainPrices.entrySet()) {
            townBalance = townBalance.subtract(BigDecimal.valueOf(priceClaimEntry.getKey()));

            if (townBalance.compareTo(BigDecimal.ZERO) < 0) {

                //Give back last claim price and set town balance
                townBalance = townBalance.add(BigDecimal.valueOf(priceClaimEntry.getKey()));
                town.setMoney(townBalance);

                return new HashMap<>(townRetainPrices.tailMap(priceClaimEntry.getKey()));
            }
        }
        town.setMoney(townBalance);
        return new HashMap<>();
    }

    /**
     * Calculate prices of retaining a town's claims
     *
     * @param town Town to calculate prices for. Only works if town has spawn on the running server
     * @return Map of claims and their prices
     */
    public TreeMap<Float, Claim> townRetainPrices(Town town) {
        TreeMap<Float, Claim> claimPrices = new TreeMap<>();
        Spawn spawn = town.getSpawn().orElse(null);
        if (spawn == null) return new TreeMap<>();//Check if town has spawn in this server
        if (!spawn.getServer().equals(huskTowns.getServerName())) return new TreeMap<>();

        for (Map.Entry<String, ClaimWorld> stringClaimWorldEntry : huskTowns.getClaimWorlds().entrySet()) {
            stringClaimWorldEntry.getValue().getClaims().entrySet().stream()
                    .filter(claimsEntry -> claimsEntry.getKey() == town.getId())
                    .map(Map.Entry::getValue)
                    .forEach(townClaims ->//Get town's claims and evaluate price for each one
                            townClaims.forEach(claim ->
                                    claimPrices.put(calculatePrice(claim, spawn.getPosition()), claim)
                            ));
        }
        return claimPrices;
    }

    private float calculatePrice(Claim claim, Position spawnPosition) {
        int chunkXCoords = claim.getChunk().getX() * 16 + 8; //Get chunk's center coords from chunk coords
        int chunkZCoords = claim.getChunk().getZ() * 16 + 8;
        int distanceX = Math.abs(chunkXCoords - (int) spawnPosition.getX());
        int distanceZ = Math.abs(chunkZCoords - (int) spawnPosition.getZ());
        float distance = (float) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceZ, 2));
        return switch (claim.getType()) {
            case FARM -> FARM_PRICE * distance;
            case PLOT -> PLOT_PRICE * distance;
            case CLAIM -> CLAIM_PRICE * distance;
        };
    }
}
