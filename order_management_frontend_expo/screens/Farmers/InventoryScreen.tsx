import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TextInput,
  Button,
  Alert,
} from "react-native";
import axios from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";

const InventoryScreen = () => {
  const [inventory, setInventory] = useState([]);
  const [name, setName] = useState("");
  const [quantity, setQuantity] = useState("");
  const [price, setPrice] = useState("");

  const fetchInventory = async () => {
    try {
      const token = await AsyncStorage.getItem("token");
      const response = await axios.get("http://http://127.0.0.1/:8000/api/inventory/", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setInventory(response.data);
    } catch (error) {
      console.error("Error fetching inventory:", error);
    }
  };

  const handleAddProduct = async () => {
    try {
      const token = await AsyncStorage.getItem("token");
      const response = await axios.post(
        "http://http://127.0.0.1/:8000/api/inventory/add/",
        { name, quantity, price },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      Alert.alert("Product Added", `Added: ${response.data.name}`);
      setName("");
      setQuantity("");
      setPrice("");
      fetchInventory(); // Refresh list after adding
    } catch (error) {
      console.error("Error adding product:", error.response?.data || error);
      Alert.alert("Error", "Failed to add product.");
    }
  };

  useEffect(() => {
    fetchInventory();
  }, []);

  const renderHeader = () => (
    <View style={[styles.row, styles.headerRow]}>
      <Text style={styles.cell}>Name</Text>
      <Text style={styles.cell}>Qty</Text>
      <Text style={styles.cell}>Price</Text>
    </View>
  );

  const renderItem = ({ item }) => (
    <View style={styles.row}>
      <Text style={styles.cell}>{item.name}</Text>
      <Text style={styles.cell}>{item.quantity}</Text>
      <Text style={styles.cell}>${item.price}</Text>
    </View>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>My Inventory</Text>

      {renderHeader()}
      <FlatList
        data={inventory}
        keyExtractor={(item) => item.id.toString()}
        renderItem={renderItem}
      />

      <Text style={styles.formTitle}>Add New Product</Text>
      <TextInput
        placeholder="Product Name"
        value={name}
        onChangeText={setName}
        style={styles.input}
      />
      <TextInput
        placeholder="Quantity"
        value={quantity}
        onChangeText={setQuantity}
        keyboardType="numeric"
        style={styles.input}
      />
      <TextInput
        placeholder="Price"
        value={price}
        onChangeText={setPrice}
        keyboardType="numeric"
        style={styles.input}
      />
      <Button title="Add Product" onPress={handleAddProduct} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  title: { fontSize: 22, fontWeight: "bold", marginBottom: 15 },
  formTitle: { fontSize: 18, marginTop: 25, marginBottom: 10 },
  input: {
    borderWidth: 1,
    borderColor: "#aaa",
    padding: 10,
    marginBottom: 10,
    borderRadius: 5,
  },
  row: {
    flexDirection: "row",
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderColor: "#ddd",
  },
  headerRow: {
    backgroundColor: "#f0f0f0",
    borderBottomWidth: 2,
  },
  cell: {
    flex: 1,
    textAlign: "center",
    fontWeight: "500",
  },
});

export default InventoryScreen;
