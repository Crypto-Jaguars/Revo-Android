import React, { useState } from "react";
import { View, Text, TextInput, Button, StyleSheet, FlatList } from "react-native";
import { useNavigation } from "@react-navigation/native";

const OrderPlacementScreen = () => {
  const navigation = useNavigation();
  const [selectedProduct, setSelectedProduct] = useState("");
  const [quantity, setQuantity] = useState("");

  const products = ["Tomatoes", "Corn", "Rice", "Bananas"];

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Place an Order</Text>

      <Text>Select a Product:</Text>
      <FlatList
        data={products}
        renderItem={({ item }) => (
          <Button title={item} onPress={() => setSelectedProduct(item)} />
        )}
      />

      <Text>Enter Quantity:</Text>
      <TextInput
        style={styles.input}
        placeholder="Enter quantity (kg)"
        keyboardType="numeric"
        value={quantity}
        onChangeText={setQuantity}
      />

      <Button
        title="Proceed to Payment"
        color="green"
        onPress={() => navigation.navigate("EscrowPayment", { selectedProduct, quantity })}
        disabled={!selectedProduct || !quantity}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  title: { fontSize: 24, fontWeight: "bold", marginBottom: 20 },
  input: { borderWidth: 1, padding: 10, marginVertical: 10, borderRadius: 5 },
});

export default OrderPlacementScreen;
