import React, { useState } from "react";
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ImageBackground } from "react-native";
import { useNavigation } from "@react-navigation/native";

const LoginScreen: React.FC = () => {
    const navigation = useNavigation();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [role, setRole] = useState("");

    const backgroundImage = { 
        uri: "https://plus.unsplash.com/premium_photo-1674624682232-c9ced5360a2e?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D" 
    };

    const handleLogin = () => {
        if (!email || !password) {
            alert("Please enter email and password");
            return;
        }

        alert("Login successful!");

        if (role.toLowerCase() === "buyer") {
            navigation.navigate("BuyerDashboard");  
        } else if (role.toLowerCase() === "farmer") {
            navigation.navigate("FarmerDashboard"); 
        } else {
            alert("Invalid role. Please enter Buyer or Farmer.");
        }
    };

    return (
        <ImageBackground source={backgroundImage} style={styles.background} resizeMode="cover">
            <View style={styles.overlay}>
                <Text style={styles.title}>Login</Text>
                <TextInput placeholder="Email" style={styles.input} onChangeText={setEmail} placeholderTextColor="#ddd"/>
                <TextInput placeholder="Password" secureTextEntry style={styles.input} onChangeText={setPassword} placeholderTextColor="#ddd"/>
                <TextInput placeholder="Role (Buyer/Farmer)" style={styles.input} onChangeText={setRole} placeholderTextColor="#ddd"/>

                <TouchableOpacity style={styles.button} onPress={handleLogin}>
                    <Text style={styles.buttonText}>Login</Text>
                </TouchableOpacity>

                <TouchableOpacity onPress={() => navigation.navigate("SignUp")}>
                    <Text style={styles.signupText}>Don't have an account? Sign Up</Text>
                </TouchableOpacity>
            </View>
        </ImageBackground>
    );
};

const styles = StyleSheet.create({
    background: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
    },
    overlay: {
        flex: 1,
        width: "100%",
        backgroundColor: "rgba(0, 0, 0, 0.5)",
        justifyContent: "center",
        alignItems: "center",
        padding: 20,
    },
    title: {
        fontSize: 24,
        fontWeight: "bold",
        color: "white",
        marginBottom: 20,
    },
    input: {
        width: "80%",
        backgroundColor: "rgba(255, 255, 255, 0.2)",
        padding: 10,
        borderRadius: 5,
        marginBottom: 10,
        color: "white",
    },
    button: {
        backgroundColor: "#28a745",
        padding: 10,
        borderRadius: 5,
        width: "80%",
        alignItems: "center",
    },
    buttonText: {
        color: "white",
        fontSize: 16,
    },
    signupText: {
        marginTop: 10,
        color: "#00c3ff",
        textDecorationLine: "underline",
    },
});

export default LoginScreen;