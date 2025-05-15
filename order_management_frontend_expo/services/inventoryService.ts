import api from "../api/api";
import AsyncStorage from "@react-native-async-storage/async-storage";

export const fetchInventory = async () => {
  const token = await AsyncStorage.getItem("token");
  const response = await api.get("/farmer/inventory/", {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

export const addInventoryItem = async (item: any) => {
  const token = await AsyncStorage.getItem("token");
  const response = await api.post("/farmer/inventory/", item, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};
