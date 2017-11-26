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
public class Snapshot {
    
    private int term; //termo do líder
    private String leaderID; //
    private int lastIncludedIndex; //todas as entradas até e incluindo este indíce são substituídas e corresponde à lastApplied to State Machine
    private int lastIncludedTerm; //termo do lastIncludedIndex
    
    
}
