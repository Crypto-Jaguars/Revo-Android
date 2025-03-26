import React from "react";
import { View, Text, Button, StyleSheet } from "react-native";
import { useNavigation } from "@react-navigation/native";

const EscrowPaymentScreen = () => {
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Secure Escrow Payment</Text>
      <Text style={styles.description}>
        Your payment will be held in escrow and released upon successful order completion.
      </Text>

      <Button title="Proceed with Payment" color="green" onPress={() => alert("Payment Processed!")} />
      <Button title="Back to Dashboard" color="gray" onPress={() => navigation.goBack()} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: "center", alignItems: "center", padding: 20 },
  title: { fontSize: 24, fontWeight: "bold", marginBottom: 10 },
  description: { fontSize: 16, textAlign: "center", marginBottom: 20 },
});

export default EscrowPaymentScreen;
