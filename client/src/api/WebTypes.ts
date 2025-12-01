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
export type GetListingsRequest = {
    categories?: string[];
    minPrice?: number;
    maxPrice?: number;
    sortBy?: string;
    sortOrder?: string;
    searchQuery?: string;
}

export type Listing = {
    userGU: string;
    listingGU: string;
    username: string;
    description: string;
    picture1_url: string;
    picture2_url: string;
    price: number;
    condition: string;
    category?: string;
    title: string;
    createdAt: string; // ISO date string
    sold: boolean;
    soldTo?: string; // UUID of the user who bought the listing
    //only update
    picture1_base64?: string;
    picture2_base64?: string;
}

//GET SPECIFIC LISTING
export type GetSpecificListingRequest = {
    listingGU: string
}

export type GetSpecificListingResponse = Listing & {
    avatar_url: string
}

//GET SPECIFIC USER LISTINGS
export type GetSpecificUserListingsRequest = {
    userGU: string
}

//UPDATE LISTING
export type UpdateListingRequest = {
    listingGU: string;
    body: Listing;
}

export type DeleteListingRequest = {
    listingGU: string;
}

//MARK LISTING AS SOLD
export type MarkListingAsSoldRequest = {
    listingGU: string;
    sold: boolean;
    soldTo?: string; // UUID of the buyer (optional)
}

export type MarkListingAsSoldResponse = Listing;

// MESSAGING

export type InitChatRoomRequest = {
    listingGU: string;
    buyerGU: string;
}

export type InitChatRoomResponse = ChatRoomDetail;

export type ChatRoom = {
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
    isSeller: boolean;
    listingSold: boolean;
}

export type ChatRoomDetail = {
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
    listingSold: boolean;
    isSeller: boolean;
}


export type MarkMessagesAsReadRequest = {
    roomID: string;
}

export type MarkMessagesAsReadResponse = {
    result: string;
}

export type Message = {
    id: string;
    roomId: string;
    senderGU: string;
    content: string;
    timestamp: string;
    isRead: boolean;
}

export type MessageRequest = {
    roomId: string;
    content: string;
}

export type UnreadCountResponse = {
    unreadCount: number;
}

//MY ACCOUNT

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
// SAVED LISTINGS
export type SaveListingRequest = {
    listingGU: string;
}

export type SaveListingResponse = {
    message: string;
}

export type UnsaveListingRequest = {
    listingGU: string;
}

export type UnsaveListingResponse = {
    message: string;
}

export type CheckSavedStatusRequest = {
    listingGU: string;
}

export type CheckSavedStatusResponse = {
    isSaved: boolean;
}

export type GetSavedListingsResponse = Listing[];

export type GetSavedListingIdsResponse = {
    savedListingIds: string[];
}

// REPORTS
export type CreateReportRequest = {
    listingGU: string;
    reason: string;
    message: string;
}

export type ReportDTO = {
    reportGU: string;
    userGU: string;
    username: string;
    listingGU: string;
    listingTitle: string;
    reason: ReportReason;
    message: string;
    status: ReportStatus;
    createdAt: string;
    reviewedByAdminGU?: string;
    reviewedAt?: string;
    adminNotes?: string;
}

export enum ReportReason {
    INAPPROPRIATE_CONTENT = "INAPPROPRIATE_CONTENT",
    SPAM = "SPAM",
    FRAUDULENT = "FRAUDULENT",
    MISLEADING_INFORMATION = "MISLEADING_INFORMATION",
    PROHIBITED_ITEM = "PROHIBITED_ITEM",
    DUPLICATE_LISTING = "DUPLICATE_LISTING",
    OFFENSIVE_LANGUAGE = "OFFENSIVE_LANGUAGE",
    OTHER = "OTHER"
}

export enum ReportStatus {
    PENDING = "PENDING",
    UNDER_REVIEW = "UNDER_REVIEW",
    RESOLVED = "RESOLVED",
    DISMISSED = "DISMISSED",
    LISTING_REMOVED = "LISTING_REMOVED"
}

export type UpdateReportRequest = {
    status: string;
    adminNotes: string;
}

export type ReportsPageResponse = {
    content: ReportDTO[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

// FOLLOWING
export type FollowUserRequest = {
    userId: string;
}

export type FollowUserResponse = {
    message: string;
    following: boolean;
}

export type UnfollowUserRequest = {
    userId: string;
}

export type UnfollowUserResponse = {
    message: string;
    following: boolean;
}

export type CheckFollowingStatusRequest = {
    userId: string;
}

export type CheckFollowingStatusResponse = {
    following: boolean;
}

export type GetFollowingListResponse = {
    following: string[];
}

export type GetFollowersListResponse = {
    followers: string[];
}

export type GetFollowCountsRequest = {
    userId: string;
}

export type GetFollowCountsResponse = {
    followers: number;
    following: number;
}

//DELETE ACCOUNT
export type DeleteAccountRequest = {
    // Empty - authentication handled by token
}

export type DeleteAccountResponse = {
    message: string;
}
