import React, { useState } from "react";
import { View, Text, TextInput, Button, StyleSheet } from "react-native";

const PlaceOrderScreen = () => {
  const [product, setProduct] = useState("");
  const [quantity, setQuantity] = useState("");

  const handleOrder = () => {
    alert(`Order placed: ${product} - ${quantity} pcs`);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Place New Order</Text>

      <TextInput
        placeholder="Product Name"
        value={product}
        onChangeText={setProduct}
        style={styles.input}
      />
      <TextInput
        placeholder="Quantity"
        value={quantity}
        onChangeText={setQuantity}
        keyboardType="numeric"
        style={styles.input}
      />
      <Button title="Submit Order" onPress={handleOrder} />
    </View>
  );
};

// Styles
const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: "#f8f8f8" },
  title: { fontSize: 24, fontWeight: "bold", marginBottom: 20 },
  input: { borderWidth: 1, padding: 10, marginBottom: 10, borderRadius: 5 },
});

export default PlaceOrderScreen;
