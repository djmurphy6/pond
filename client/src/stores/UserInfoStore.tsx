import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

export type UserInfo = {
    userGU: string;
    username: string;  // Matches backend field name
    email: string;
    avatar_url: string;  // Matches backend field name
    bio: string;
    userScore?: number;  // Made optional since backend doesn't return this
    admin: boolean;
}

type UserInfoStore = {
    userInfo?: UserInfo;
    setUserInfo: (userInfo?: UserInfo) => void;
}

export const useUserInfoStore = create<UserInfoStore>()(
    persist(
        (set) => ({
            userInfo: undefined,
            setUserInfo: (userInfo?: UserInfo) => set({ userInfo }),
        }),
        {
            name: "user-info-store",
            storage: createJSONStorage(() => sessionStorage),
        }
    )
);
