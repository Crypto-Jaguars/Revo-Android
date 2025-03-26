import React from "react";
import { View, Text, FlatList, StyleSheet } from "react-native";

const orders = [
  { id: "1", buyer: "John Doe", status: "Pending" },
  { id: "2", buyer: "Jane Smith", status: "Pending" },
];

const OrdersToProcessScreen = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Orders to Process</Text>
      <FlatList
        data={orders}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={styles.orderItem}>
            <Text>Buyer: {item.buyer}</Text>
            <Text>Status: {item.status}</Text>
          </View>
        )}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  title: {
    fontSize: 22,
    fontWeight: "bold",
    marginBottom: 10,
  },
  orderItem: {
    padding: 15,
    backgroundColor: "#f9f9f9",
    marginBottom: 10,
    borderRadius: 8,
  },
});

export default OrdersToProcessScreen;
