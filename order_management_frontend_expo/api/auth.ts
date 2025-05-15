import axios from "axios";

const API_BASE_URL = "http://127.0.0.1:8000/api/auth";

export const storeTokens = (access: string, refresh: string) => {
    localStorage.setItem("access", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzQzMDg0Njk1LCJpYXQiOjE3NDMwODQzOTUsImp0aSI6IjUzZDhlN2I2OGMxYjRlMjY5M2E5ZjliZjIxYzA1ODJlIiwidXNlcl9pZCI6MX0.ivdVq56eT9r7QjHhFtDu8g9uMge9ct59ZnOOGSLzusM");
    localStorage.setItem("refresh", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc0MzE3MDc5NSwiaWF0IjoxNzQzMDg0Mzk1LCJqdGkiOiIwZDY4ZTdhYzcwNmE0OTZiOWMzNTgyNjgwNDE4ZDBmZCIsInVzZXJfaWQiOjF9.oJrtQueAdmzaSOFTFZvE-4gsHEk9Y06l6K3hQJune4I");
};

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

        // Extract tokens if returned
        if (response.data.access && response.data.refresh) {
            storeTokens(response.data.access, response.data.refresh);
        }

        return response.data; 
    } catch (error: any) {
        throw new Error(error.response?.data?.message || "Signup failed.");
    }
};

export const login = async (email: string, password: string) => {
    try {
        console.log("Attempting login..."); 

        const response = await axios.post(
            `${API_BASE_URL}/login/`,
            { email, password },
            { headers: { "Cache-Control": "no-cache" } } 
        );

        console.log("Login successful, tokens received:", response.data); 

        if (response.data.access && response.data.refresh) {
            storeTokens(response.data.access, response.data.refresh);
        }

        return response.data;
    } catch (error: any) {
        console.error("Login failed:", error.response?.data);
        throw new Error(error.response?.data?.message || "Login failed.");
    }
};

export const refreshAccessToken = async (): Promise<string | null> => {
    try {
        const refreshToken = localStorage.getItem("refresh");
        if (!refreshToken) throw new Error("No refresh token available");

        const response = await axios.post(`${API_BASE_URL}/refresh/`, { refresh: refreshToken });

        const newAccessToken = response.data.access;
        localStorage.setItem("access", newAccessToken); 

        return newAccessToken;
    } catch (error) {
        console.error("Error refreshing token:", error);
        return null;
    }
};

export const logout = () => {
    localStorage.removeItem("access");
    localStorage.removeItem("refresh");
};
