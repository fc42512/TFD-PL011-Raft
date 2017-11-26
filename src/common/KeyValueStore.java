/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class KeyValueStore {

    private TreeMap<String, String> keyValueStore;

    public KeyValueStore() {
        this.keyValueStore = new TreeMap<>();
    }

    public void put(String key, String value) {
        keyValueStore.put(key, value);
    }

    public String get(String key, String value) {
        StringBuilder response = new StringBuilder();
        String currentValue = keyValueStore.get(key);
        if (currentValue == null) {
            response.append("Não existe valor associado a esta KEY!!");
        } else {
            response.append("A key " + key + " tem o valor " + currentValue);
        }
        keyValueStore.put(key, value);
        return response.toString();
    }

    public String delete(String key, String value) {
        StringBuilder response = new StringBuilder();
        String currentValue = keyValueStore.get(key);
        if (currentValue == null) {
            response.append("Não é possível apagar esta KEY, pois não existe!!");
        } else {
            
            response.append("A key " + key + " com o valor " + currentValue + " foi apagada");
        }
        keyValueStore.put(key, value);
        return response.toString();
    }

    public String compareAndSwap(String key, String oldValue, String newValue) {
        StringBuilder response = new StringBuilder();
        String value = keyValueStore.get(key);
        if (value != null) {
            if (Objects.equals(value, oldValue)) {
                put(key, newValue);
                response.append("Os valores comparados são iguais. A valor foi substituído!!");
            } else {
                response.append("Os valores comparados não são iguais. A valor foi não substituído!!");
            }
        } else {
            response.append("Não é possível comparar os valores, pois esta KEY não tem valor associado!!");
        }
        return response.toString();
    }

    public String list(String key, String value) {
        StringBuilder response = new StringBuilder("Chave---Valor\n");
        keyValueStore.put(key, value);
        response.append("-> " + key + " - " + keyValueStore.get(key) + "\n");
        return response.toString();
    }

    public String getStateMachineState() {
        StringBuilder response = new StringBuilder("STATE_MACHINE_STATE\n");
        for (Map.Entry<String, String> keyValue : keyValueStore.entrySet()) {
            response.append(keyValue.getKey() + ";" + keyValue.getValue() + "\n");
        }
        return response.toString();
    }
}
