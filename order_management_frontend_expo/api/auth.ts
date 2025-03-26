import axios from "axios";

const API_BASE_URL = "http://127.0.0.1:8000/api/auth";

export const signup = async (userData: {
    email: string;
    fullName: string;
    nationality: string;
    role: string;
    password: string;
}) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/signup/`, {
            username: userData.email.split("@")[0], 
            email: userData.email,
            full_name: userData.fullName,
            nationality: userData.nationality,
            role: userData.role.toLowerCase(),
            password: userData.password,
        });

        return response.data; 
    } catch (error: any) {
        throw new Error(error.response?.data?.message || "Signup failed.");
    }
};
