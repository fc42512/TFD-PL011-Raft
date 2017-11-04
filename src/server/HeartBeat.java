/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author João
 */
public class HeartBeat implements Runnable {

    private Leader leader;

    public HeartBeat(Leader leader) {
        this.leader = leader;
    }

    @Override
    public void run() {
        try {
            while (true) {
                AppendEntry ae = new AppendEntry(leader.getServer().getCurrentTerm(), leader.getServer().getServerID(), 0, 0, null, leader.getServer().getCommitIndex(), true, null, "HEARTBEAT");
                leader.sendAppendEntries(ae);
                System.out.println("Enviados os heartbeats para todos os followers...");
                
                Thread.sleep(15000);
            }
        } catch (InterruptedException ex) {
            System.out.println("Erro no envio dos heartbeats!");
        }
    }

}
