package com.example.monitor;

public class DataManager {
    public static boolean MainFragmentRequests;
    public static boolean SettingsFragmentRequests;
    public static boolean InfoFragmentRequests;

    public static void requestDataView(DataPackages dataPackages, BluetoothLayer bluetoothLayer, DataPackageType datapack) {
        // request current DataView from DataPackages
        DataView currentDataView = null;
        switch (datapack) {
            case INFO_FRAGMENT_DATA_PACKAGE: {
                currentDataView = dataPackages.getInfoFragmentDataView();
                break;
            }

            case SETTINGS_FRAGMENT_DATA_PACKAGE: {
                currentDataView = dataPackages.getSettingsFragmentDataView();
                break;
            }

            case MAIN_FRAGMENT_DATA_PACKAGE: {
                currentDataView = dataPackages.getMainFragmentDataView();
                break;
            }

        }

        assert currentDataView != null;

        if (currentDataView.table == DeviceProtocol.READ_TABLE) {

            switch (currentDataView.size) {
                case 1: {
                    // read byte
                    readCommand(bluetoothLayer, DeviceProtocol.READ_BYTE_COMMAND, currentDataView.address);
                    break;
                }

                case 2: {
                    // read word
                    readCommand(bluetoothLayer, DeviceProtocol.READ_WORD_COMMAND, currentDataView.address);
                    break;
                }

                case 4: {
                    // read dword
                    readCommand(bluetoothLayer, DeviceProtocol.READ_DWORD_COMMAND, currentDataView.address);
                    break;
                }
            }

        } else if (currentDataView.table == DeviceProtocol.READ_WRITE_TABLE) {

            switch (currentDataView.size) {
                case 1: {
                    // read byte
                    readCommand(bluetoothLayer, DeviceProtocol.READ_READWRITE_BYTE_COMMAND, currentDataView.address);
                    break;
                }

                case 2: {
                    // read word
                    readCommand(bluetoothLayer, DeviceProtocol.READ_READWRITE_WORD_COMMAND, currentDataView.address);
                    break;
                }

                case 4: {
                    // read dword
                    readCommand(bluetoothLayer, DeviceProtocol.READ_READWRITE_DWORD_COMMAND, currentDataView.address);
                    break;
                }
            }

        }
    }

    private static void readCommand(BluetoothLayer bluetoothLayer, byte command, int address) {
        bluetoothLayer.writeRXCharacteristic(
                makeRequestPackage(
                        command,
                        new byte[] {(byte) address}
                )
        );
    }

    public static byte[] makeRequestPackage(byte command, byte[] commandBody) {
        byte[] result = new byte[1 + commandBody.length];
        result[0] = command;
        System.arraycopy(commandBody, 0, result, 1, commandBody.length);
        return result;
    }

    /*
     * check if given response provides error code
     * */
    public static boolean isErrorResponse(byte response) {
        return response < 0;
    }

    /*
     * extract command gave error from error answer
     * */
    public static byte extractErrorCommand(byte response) {
        return (byte)(response & 0x7f);
    }
}
