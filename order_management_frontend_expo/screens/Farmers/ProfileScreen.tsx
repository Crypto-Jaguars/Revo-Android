import React, { useEffect, useState } from "react";
import { View, Text, TextInput, Button, Image, StyleSheet, ActivityIndicator, Alert } from "react-native";
import * as ImagePicker from "expo-image-picker";
import api from "../../api/api";

const ProfileScreen = () => {
  const [profile, setProfile] = useState({
    name: "",
    phone: "",
    location: "",
    photo: "",
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get("/farmer/profile/")
      .then((res) => {
        setProfile(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Failed to load profile", err);
        setLoading(false);
      });
  }, []);

  const pickImage = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      quality: 1,
    });

    if (!result.canceled) {
      setProfile({ ...profile, photo: result.assets[0].uri });
    }
  };

  const saveProfile = () => {
    setLoading(true);
    api.put("/farmer/profile/", profile)
      .then(() => {
        Alert.alert("Success", "Profile updated");
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        Alert.alert("Error", "Failed to update profile");
        setLoading(false);
      });
  };

  if (loading) {
    return <ActivityIndicator size="large" style={{ marginTop: 50 }} />;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.label}>Name</Text>
      <TextInput style={styles.input} value={profile.name} onChangeText={(text) => setProfile({ ...profile, name: text })} />

      <Text style={styles.label}>Phone</Text>
      <TextInput style={styles.input} value={profile.phone} onChangeText={(text) => setProfile({ ...profile, phone: text })} />

      <Text style={styles.label}>Location</Text>
      <TextInput style={styles.input} value={profile.location} onChangeText={(text) => setProfile({ ...profile, location: text })} />

      {profile.photo ? (
        <Image source={{ uri: profile.photo }} style={styles.image} />
      ) : (
        <Text style={styles.label}>No photo uploaded</Text>
      )}
      <Button title="Pick Photo" onPress={pickImage} />

      <View style={{ marginTop: 20 }}>
        <Button title="Save Profile" onPress={saveProfile} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 20,
    backgroundColor: "#fff",
    flex: 1,
  },
  label: {
    marginTop: 10,
    fontWeight: "bold",
  },
  input: {
    borderColor: "#ccc",
    borderWidth: 1,
    padding: 8,
    marginTop: 5,
  },
  image: {
    width: 100,
    height: 100,
    marginTop: 10,
  },
});

export default ProfileScreen;
