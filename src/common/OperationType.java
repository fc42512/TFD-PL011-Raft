/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.Random;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public enum OperationType {
    PUT,
    GET,
    DEL,
    LIST,
    CAS;

    public static OperationType getRandomOperation() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
