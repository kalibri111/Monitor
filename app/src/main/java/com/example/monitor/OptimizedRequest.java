package com.example.monitor;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class OptimizedRequest {
    private List<DataView> bufferForRequest = new ArrayList<DataView>();

    OptimizedRequest(DataPackages dataPackages, List<DataView> dataPackagesDataPack) {
        int current_index = dataPackages.getCurrent();

        bufferForRequest.add(dataPackagesDataPack.get(current_index));
        current_index += 1;

        if (current_index == dataPackagesDataPack.size()) {
            current_index = 0;
        }


        while (
                dataPackagesDataPack.get(current_index).address - 1 == bufferForRequest.get(bufferForRequest.size() - 1).address &&
                dataPackagesDataPack.get(current_index).table == bufferForRequest.get(bufferForRequest.size() - 1).table
        ) {
            bufferForRequest.add(dataPackagesDataPack.get(current_index));
            Log.e("Monitor", String.format("current index: %s", current_index));
            if (current_index + 1 < dataPackagesDataPack.size()) {
                current_index += 1;
            } else {
                current_index = 0;
                break;
            }
        }

        if (current_index == dataPackagesDataPack.size()) {
            current_index = 0;
        }

        Log.e("Monitor", String.format("current index: %s", current_index));
        dataPackages.setCurrent(current_index);
    }

    public List<DataView> getBufferForRequest() {
        return bufferForRequest;
    }
}
