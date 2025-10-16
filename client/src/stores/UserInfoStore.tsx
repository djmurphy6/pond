import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

export type UserInfo = {
    userGU: string;
    userName: string;
    email: string;
    avatarURL: string;
    bio: string;
    userScore: number;
    userType: boolean;
}

type UserInfoStore = {
    userInfo?: UserInfo;
    setUserInfo: (userInfo?: UserInfo) => void;
}

export const useUserInfoStore = create<UserInfoStore>()(
    persist(
        (set) => ({
            userInfo: undefined,
            setUserInfo: (userInfo) => set({ userInfo }),
        }),
        {
            name: "user-info-store",
            storage: createJSONStorage(() => sessionStorage),
        }
    )
);
