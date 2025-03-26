import React, { useState } from "react";
import { View, Text, Image, Button, StyleSheet, TouchableOpacity } from "react-native";
import { useNavigation } from "@react-navigation/native";

const ProfileScreen = () => {
  const navigation = useNavigation();

  const [user, setUser] = useState({
    profilePicture: "https://via.placeholder.com/150",
    fullName: "john d",
    phoneNumber: "+1234567890",
    location: "Lagos, Nigeria",
    sex: "Male"
  });

  const handleLogout = () => {
    console.log("Logging out...");
    navigation.navigate("LoginScreen"); //
  };

  return (
    <View style={styles.container}>
      {/* Profile Picture */}
      <Image source={{ uri: user.profilePicture }} style={styles.profileImage} />

      {/* User Info */}
      <Text style={styles.fullName}>{user.fullName}</Text>
      <Text style={styles.info}>📞 {user.phoneNumber}</Text>
      <Text style={styles.info}>📍 {user.location}</Text>
      <Text style={styles.info}>⚤ {user.sex}</Text>

      {/* Edit Profile Button */}
      <TouchableOpacity style={styles.editButton}>
        <Text style={styles.editButtonText}>Edit Profile</Text>
      </TouchableOpacity>

      {/* Logout Button */}
      <Button title="Logout" color="red" onPress={handleLogout} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    padding: 20,
    backgroundColor: "#f9f9f9"
  },
  profileImage: {
    width: 120,
    height: 120,
    borderRadius: 60,
    marginBottom: 15
  },
  fullName: {
    fontSize: 22,
    fontWeight: "bold",
    marginBottom: 5
  },
  info: {
    fontSize: 16,
    color: "#555",
    marginBottom: 5
  },
  editButton: {
    marginTop: 15,
    backgroundColor: "#007bff",
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 5
  },
  editButtonText: {
    color: "#fff",
    fontSize: 16
  }
});

export default ProfileScreen;
