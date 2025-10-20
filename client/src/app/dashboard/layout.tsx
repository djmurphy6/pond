"use client";

//React
import { useEffect } from "react";
import { useRouter } from "next/navigation";

//API
import api from "@/api/WebService";

//Internal
import { useUserInfoStore } from "@/stores/UserInfoStore";
import { ErrorResponse } from "@/api/WebTypes";


export default function DashboardLayout({ children }: { children: React.ReactNode }) {
    const router = useRouter();
    const { setUserInfo } = useUserInfoStore();

    useEffect(() => {
        const getUserInfo = async () => {
            const userInfo = await api.GetUserInfo({});
            console.log("GetUserInfo:", JSON.stringify(userInfo));
            if (userInfo instanceof ErrorResponse) {
                router.push("/login?error=unauthorized");
                return;
            } else {
                setUserInfo(userInfo);
            }
        }
        getUserInfo();
    }, []);

    return <>{children}</>;
}
