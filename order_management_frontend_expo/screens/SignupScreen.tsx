import React, { useState } from "react";
import { 
    View, Text, TextInput, TouchableOpacity, StyleSheet, ImageBackground 
} from "react-native";
import { useNavigation } from "@react-navigation/native";
import { signup } from "../api/auth"; 

const SignupScreen: React.FC = () => {
    const navigation = useNavigation();

    const [form, setForm] = useState({
        email: "",
        fullName: "",
        nationality: "",
        role: "",
        password: "",
        confirmPassword: "",
    });

    const backgroundImage = { 
        uri: "https://plus.unsplash.com/premium_photo-1674624682232-c9ced5360a2e?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    };

    const handleSignup = async () => {
        if (form.password !== form.confirmPassword) {
            alert("Passwords do not match!");
            return;
        }

        try {
            const response = await signup({
                email: form.email,
                fullName: form.fullName,
                nationality: form.nationality,
                role: form.role,
                password: form.password,
            });

            if (response.success) {
                alert("Signup successful!");
                navigation.navigate("Login" as never);
            } else {
                alert(response.message || "Signup failed.");
            }
        } catch (error: any) {
            alert(error.message || "Signup failed. Please try again.");
        }
    };

    return (
        <ImageBackground source={backgroundImage} style={styles.background} resizeMode="cover">
            <View style={styles.overlay}>
                <Text style={styles.title}>Signup</Text>

                <TextInput 
                    placeholder="Email" 
                    style={styles.input} 
                    onChangeText={(text) => setForm({ ...form, email: text })} 
                    placeholderTextColor="#ddd"
                />
                <TextInput 
                    placeholder="Full Name" 
                    style={styles.input} 
                    onChangeText={(text) => setForm({ ...form, fullName: text })} 
                    placeholderTextColor="#ddd"
                />
                <TextInput 
                    placeholder="Nationality" 
                    style={styles.input} 
                    onChangeText={(text) => setForm({ ...form, nationality: text })} 
                    placeholderTextColor="#ddd"
                />
                <TextInput 
                    placeholder="Role (Buyer/Farmer)" 
                    style={styles.input} 
                    onChangeText={(text) => setForm({ ...form, role: text })} 
                    placeholderTextColor="#ddd"
                />
                <TextInput 
                    placeholder="Password" 
                    secureTextEntry 
                    style={styles.input} 
                    onChangeText={(text) => setForm({ ...form, password: text })} 
                    placeholderTextColor="#ddd"
                />
                <TextInput 
                    placeholder="Confirm Password" 
                    secureTextEntry 
                    style={styles.input} 
                    onChangeText={(text) => setForm({ ...form, confirmPassword: text })} 
                    placeholderTextColor="#ddd"
                />

                <TouchableOpacity style={styles.button} onPress={handleSignup}>
                    <Text style={styles.buttonText}>Sign Up</Text>
                </TouchableOpacity>

                <TouchableOpacity onPress={() => navigation.navigate("Login" as never)}>
                    <Text style={styles.signupText}>Already have an account? Login</Text>
                </TouchableOpacity>
            </View>
        </ImageBackground>
    );
};

// Styles
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
        backgroundColor: "#007BFF",
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

export default SignupScreen;
