export class ErrorResponse {
    name?: string;
    code?: number;
    statusText?: string;
    body?: { error: string };

    constructor(name: string, code: number, statusText: string, body?: { error: string }) {
        this.name = name;
        this.code = code;
        this.statusText = statusText;
        this.body = body;
    }
}

// REGISTER
export type RegisterUserRequest = {
    email: string
    password: string
    username: string
}

export type RegisterUserResponse = {
    //Empty
}

//LOGIN
export type LoginRequest = {
    email: string;
    password: string;
}
export type LoginResponse = {
    accessToken: string;
}

//VERIFY USER
export type VerifyUserRequest = {
    email: string;
    verificationCode: string;
}
export type VerifyUserResponse = {
    message: string;
}

//GET USER INFO
export type GetUserInfoRequest = {
    // email: string;
}

//CREATE LISTING
export type CreateListingRequest = {
    description: string;
    picture1_base64?: string;
    picture2_base64?: string;
    price: number;
    condition: string;
    category?: string;
    title: string;
}
export type CreateListingResponse = Listing

//GET LISTINGS
export type Listing = {
    userGU: string;
    listingGU: string;
    description: string;
    picture1_url: string;
    picture2_url: string;
    price: number;
    condition: string;
    category?: string;
    title: string;
    //only update
    picture1_base64?: string;
    picture2_base64?: string;
}

//GET SPECIFIC LISTING
export type GetSpecificListingRequest = {
    listingGU: string
}

//UPDATE LISTING
export type UpdateListingRequest = {
    listingGU: string;
    body: Listing;
}

export type DeleteListingRequest = {
    listingGU: string;
}

// MESSAGING
export type ChatRoomListDTO = {
    roomId: string;
    listingGU: string;
    listingTitle: string;
    listingImage: string;
    otherUserGU: string;
    otherUsername: string;
    otherUserAvatar: string;
    lastMessage: string;
    lastMessageAt: string;
    unreadCount: number;
}

export type ChatRoomDetailDTO = {
    roomId: string;
    listingGU: string;
    listingTitle: string;
    listingPrice: number;
    listingImage: string;
    otherUserGU: string;
    otherUsername: string;
    otherUserAvatar: string;
    createdAt: string;
    lastMessageAt: string;
}

export type MessageResponseDTO = {
    id: string;
    roomId: string;
    senderGU: string;
    content: string;
    timestamp: string;
    isRead: boolean;
}

export type UpdateUserRequest = {
    username?: string;
    bio?: string;
}

export type UpdateUserResponse = {
    userGU: string;
    username: string;
    email: string;
    avatar_url: string;
    bio: string;
    admin: boolean;
}


export type UploadAvatarRequest = {
    avatar_base64: string;
}

export type UploadAvatarResponse = {
    avatar_url: string;
}
