import { StyleSheet } from "react-native";

// Define colors
export const colors = {
  primary: "#0A3062", // Deep Blue
  secondary: "#82CDDD", // Sky Blue
  farmerPrimary: "#1E8449", // Green Tint
  farmerSecondary: "#A9DFBF", // Light Green
  buyerPrimary: "#A9D809", // Blue Tint
  buyerSecondary: "#A4D6F1", // Soft Blue
  chatDark: "#2C3E50", // Dark Mode Background
  chatLight: "#BDC3C7", // Light Messages
  textPrimary: "#FFFFFF",
  textSecondary: "#F1F1F1",
};

// Global Styles
export const globalStyles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.primary, // Default background
  },
  titleText: {
    fontSize: 22,
    color: colors.textPrimary,
    fontWeight: "bold",
  },
});
