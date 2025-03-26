import React, { useState } from "react";
import { View, Text, TextInput, Button, StyleSheet } from "react-native";

const FarmerProfileScreen = () => {
  const [name, setName] = useState("John Doe");
  const [farmLocation, setFarmLocation] = useState("Green Valley Farm");
  const [contact, setContact] = useState("+123 456 7890");

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Farmer Profile</Text>

      <Text style={styles.label}>Name</Text>
      <TextInput style={styles.input} value={name} onChangeText={setName} />

      <Text style={styles.label}>Farm Location</Text>
      <TextInput style={styles.input} value={farmLocation} onChangeText={setFarmLocation} />

      <Text style={styles.label}>Contact</Text>
      <TextInput style={styles.input} value={contact} onChangeText={setContact} />

      <Button title="Save Changes" onPress={() => alert("Profile Updated!")} />
      <Button title="Logout" color="red" onPress={() => alert("Logging Out...")} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, justifyContent: "center" },
  title: { fontSize: 24, fontWeight: "bold", textAlign: "center", marginBottom: 20 },
  label: { fontSize: 16, marginTop: 10 },
  input: { borderWidth: 1, padding: 10, marginTop: 5, borderRadius: 5 },
});

export default FarmerProfileScreen;
