"use client";

//Next
import Link from "next/link";
import { useTheme } from "next-themes";

//React
import { useEffect, useState } from "react";
import { Shield, AlertTriangle, Eye, CheckCircle, XCircle, Clock, Trash2, ArrowLeft, Menu } from "lucide-react";
import { toast } from "sonner";

//API
import api from "@/api/WebService";
import { ErrorResponse, ReportDTO, ReportStatus, ReportReason } from "@/api/WebTypes";

//ShadCN
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Select, SelectContent, SelectItem, SelectValue, SelectTrigger } from "@/components/ui/select";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";

//Internal
import ThemeToggle from "@/components/ThemeToggle";
import { useUserInfoStore } from "@/stores/UserInfoStore";
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";
import MobileHeader from "@/components/MobileHeader";
import { SideBarAside } from "@/components/SideBarAside";

const REPORT_REASON_LABELS: Record<ReportReason, string> = {
    [ReportReason.INAPPROPRIATE_CONTENT]: "Inappropriate Content",
    [ReportReason.SPAM]: "Spam",
    [ReportReason.FRAUDULENT]: "Fraudulent/Scam",
    [ReportReason.MISLEADING_INFORMATION]: "Misleading Information",
    [ReportReason.PROHIBITED_ITEM]: "Prohibited Item",
    [ReportReason.DUPLICATE_LISTING]: "Duplicate Listing",
    [ReportReason.OFFENSIVE_LANGUAGE]: "Offensive Language",
    [ReportReason.OTHER]: "Other"
};

const STATUS_COLORS: Record<ReportStatus, string> = {
    [ReportStatus.PENDING]: "bg-yellow-500/20 text-yellow-700 dark:text-yellow-400",
    [ReportStatus.UNDER_REVIEW]: "bg-blue-500/20 text-blue-700 dark:text-blue-400",
    [ReportStatus.RESOLVED]: "bg-green-500/20 text-green-700 dark:text-green-400",
    [ReportStatus.DISMISSED]: "bg-gray-500/20 text-gray-700 dark:text-gray-400",
    [ReportStatus.LISTING_REMOVED]: "bg-red-500/20 text-red-700 dark:text-red-400"
};

export default function AdminReportsPage() {
    const [reports, setReports] = useState<ReportDTO[]>([]);
    const [mounted, setMounted] = useState(false);
    const [loading, setLoading] = useState(true);
    const { theme } = useTheme();
    const { userInfo } = useUserInfoStore();

    const [selectedReport, setSelectedReport] = useState<ReportDTO | null>(null);
    const [isReviewDialogOpen, setIsReviewDialogOpen] = useState(false);
    const [newStatus, setNewStatus] = useState<string>("");
    const [adminNotes, setAdminNotes] = useState("");
    const [isUpdating, setIsUpdating] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [showSidebar, setShowSidebar] = useState(false);

    useEffect(() => {
        setMounted(true);
        if (userInfo?.admin) {
            fetchReports();
        }
    }, [currentPage, userInfo]);

    const fetchReports = async () => {
        setLoading(true);
        const res = await api.GetAllReports(currentPage, 20);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error || "Failed to load reports");
        } else {
            setReports(res.content);
            setTotalPages(res.totalPages);
        }
        setLoading(false);
    };

    const handleReviewClick = (report: ReportDTO) => {
        setSelectedReport(report);
        setNewStatus(report.status);
        setAdminNotes(report.adminNotes || "");
        setIsReviewDialogOpen(true);
    };

    const handleUpdateReport = async () => {
        if (!selectedReport || !newStatus) return;

        setIsUpdating(true);
        const res = await api.UpdateReport(selectedReport.reportGU, {
            status: newStatus,
            adminNotes: adminNotes
        });

        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error || "Failed to update report");
        } else {
            toast.success("Report updated successfully");
            setIsReviewDialogOpen(false);
            fetchReports();
        }
        setIsUpdating(false);
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString() + " " + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    if (!mounted) return null;

    if (!userInfo?.admin) {
        return (
            <div className="flex h-screen items-center justify-center bg-background">
                <Card className="max-w-md">
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2 text-red-600">
                            <Shield className="h-6 w-6" />
                            Access Denied
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-muted-foreground">You don't have permission to access this page.</p>
                        <Link href="/dashboard">
                            <Button className="mt-4 w-full">Return to Dashboard</Button>
                        </Link>
                    </CardContent>
                </Card>
            </div>
        );
    }

    const SideBar = () => (
        <SideBarAside>
            <Button variant={'link'} style={{ color: 'gray', justifyContent: 'flex-start' }} className="!p-0 !px-0 !py-0 hover:underline hover:bg-none cursor-pointer mb-4">
                <Link style={{ flexDirection: 'row' }} className="flex items-center gap-1" href="/dashboard">
                    <ArrowLeft />
                    back to dashboard
                </Link>
            </Button>

            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                    <Shield className="h-6 w-6 text-amber-600" />
                    <h2 className="text-xl font-semibold">Admin Panel</h2>
                </div>
                {/* <ThemeToggle /> */}
            </div>

            <Separator className="my-4" />

            <div className="mt-auto pt-6">
                <p className="text-xs text-muted-foreground">
                    Logged in as <span className="font-medium">@{userInfo.username}</span>
                </p>
            </div>
        </SideBarAside>
    )

    return (
        <div className="flex flex-col md:flex-row h-screen bg-background transition-colors duration-300">
            {/* Sidebar */}
            <div className="hidden md:flex ">
                <SideBar />
            </div>

            <Sheet open={showSidebar} onOpenChange={setShowSidebar}>
                <SheetTitle className="sr-only">Admin Panel</SheetTitle>
                <SheetContent side="left" className="w-64">
                    <SideBar />
                </SheetContent>
            </Sheet>

            <MobileHeader onPress={setShowSidebar} />

            {/* Main Content */}
            <main className="flex-1 flex flex-col">
                <div className={`sticky top-0 z-10 p-6 border-b bg-muted/40 backdrop-blur-sm transition-colors duration-300 ${theme !== "dark" && "shadow-sm"
                                }`}>
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold flex items-center gap-2">
                                <AlertTriangle className="h-8 w-8 text-amber-600" />
                                Listing Reports
                            </h1>
                            <p className="text-muted-foreground mt-1">
                                Review and manage user-submitted reports
                            </p>
                        </div>
                    </div>
                </div>

                <ScrollArea className="flex-1 p-6">
                    {loading ? (
                        <div className="space-y-4">
                            {[1, 2, 3, 4, 5].map((i) => (
                                <Card key={i}>
                                    <CardContent className="p-6">
                                        <Skeleton className="h-20 w-full" />
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    ) : reports.length === 0 ? (
                        <Card>
                            <CardContent className="p-12 text-center">
                                <CheckCircle className="h-16 w-16 mx-auto text-green-500 mb-4" />
                                <h3 className="text-xl font-semibold mb-2">No Reports</h3>
                                <p className="text-muted-foreground">
                                    All reports have been reviewed or there are no reports to show.
                                </p>
                            </CardContent>
                        </Card>
                    ) : (
                        <div className="space-y-4">
                            {reports.map((report) => (
                                <Card key={report.reportGU} className="hover:shadow-md transition-shadow">
                                    <CardContent className="p-6">
                                        <div className="flex items-start justify-between">
                                            <div className="flex-1">
                                                <div className="flex items-center gap-2 mb-2">
                                                    <span className={`px-2 py-1 rounded-md text-xs font-medium ${STATUS_COLORS[report.status]}`}>
                                                        {report.status.replace(/_/g, ' ')}
                                                    </span>
                                                    <span className="text-xs text-muted-foreground">
                                                        {formatDate(report.createdAt)}
                                                    </span>
                                                </div>

                                                <div className="space-y-1 mb-3">
                                                    <h3 className="font-semibold text-lg">
                                                        {report.listingTitle || "Deleted Listing"}
                                                    </h3>
                                                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                                                        <span>Reported by: <span className="font-medium text-foreground">@{report.username}</span></span>
                                                        <Separator orientation="vertical" className="h-4" />
                                                        <span>Reason: <span className="font-medium text-foreground">{REPORT_REASON_LABELS[report.reason]}</span></span>
                                                    </div>
                                                </div>

                                                {report.message && (
                                                    <div className="bg-muted/50 rounded-md p-3 mb-3">
                                                        <p className="text-sm">{report.message}</p>
                                                    </div>
                                                )}

                                                {report.adminNotes && (
                                                    <div className="bg-amber-500/10 rounded-md p-3 border border-amber-500/20">
                                                        <p className="text-xs font-medium text-amber-700 dark:text-amber-400 mb-1">Admin Notes:</p>
                                                        <p className="text-sm">{report.adminNotes}</p>
                                                        {report.reviewedAt && (
                                                            <p className="text-xs text-muted-foreground mt-1">
                                                                Reviewed on {formatDate(report.reviewedAt)}
                                                            </p>
                                                        )}
                                                    </div>
                                                )}
                                            </div>

                                            <div className="flex flex-col gap-2 ml-4">
                                                <Link href={`/dashboard/listing/${report.listingGU}`} target="_blank">
                                                    <Button variant="outline" size="sm" className="cursor-pointer">
                                                        <Eye className="h-4 w-4 mr-2" />
                                                        View Listing
                                                    </Button>
                                                </Link>
                                                <Button
                                                    variant="default"
                                                    size="sm"
                                                    className="cursor-pointer bg-uo-green text-white hover:bg-uo-green/90"
                                                    onClick={() => handleReviewClick(report)}
                                                >
                                                    <CheckCircle className="h-4 w-4 mr-2" />
                                                    Review
                                                </Button>
                                            </div>
                                        </div>
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    )}

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex justify-center gap-2 mt-6">
                            <Button
                                variant="outline"
                                onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                                disabled={currentPage === 0}
                                className="cursor-pointer"
                            >
                                Previous
                            </Button>
                            <span className="flex items-center px-4 text-sm text-muted-foreground">
                                Page {currentPage + 1} of {totalPages}
                            </span>
                            <Button
                                variant="outline"
                                onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={currentPage === totalPages - 1}
                                className="cursor-pointer"
                            >
                                Next
                            </Button>
                        </div>
                    )}
                </ScrollArea>
            </main>

            {/* Review Dialog */}
            <Dialog open={isReviewDialogOpen} onOpenChange={setIsReviewDialogOpen}>
                <DialogContent className="sm:max-w-lg">
                    <DialogHeader>
                        <DialogTitle>Review Report</DialogTitle>
                    </DialogHeader>

                    {selectedReport && (
                        <div className="space-y-4">
                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">Listing</Label>
                                <p className="font-medium">{selectedReport.listingTitle || "Deleted Listing"}</p>
                            </div>

                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">Reported by</Label>
                                <p className="font-medium">@{selectedReport.username}</p>
                            </div>

                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">Reason</Label>
                                <p className="font-medium">{REPORT_REASON_LABELS[selectedReport.reason]}</p>
                            </div>

                            {selectedReport.message && (
                                <div>
                                    <Label className="text-sm font-medium text-muted-foreground">Details</Label>
                                    <p className="text-sm bg-muted/50 rounded-md p-3">{selectedReport.message}</p>
                                </div>
                            )}

                            <Separator />

                            <div className="space-y-2">
                                <Label htmlFor="status">Update Status *</Label>
                                <Select value={newStatus} onValueChange={setNewStatus}>
                                    <SelectTrigger>
                                        <SelectValue placeholder="Select status" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value={ReportStatus.PENDING}>Pending</SelectItem>
                                        <SelectItem value={ReportStatus.UNDER_REVIEW}>Under Review</SelectItem>
                                        <SelectItem value={ReportStatus.RESOLVED}>Resolved</SelectItem>
                                        <SelectItem value={ReportStatus.DISMISSED}>Dismissed</SelectItem>
                                        <SelectItem value={ReportStatus.LISTING_REMOVED}>Listing Removed</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="adminNotes">Admin Notes</Label>
                                <Textarea
                                    id="adminNotes"
                                    value={adminNotes}
                                    onChange={(e) => setAdminNotes(e.target.value)}
                                    placeholder="Add notes about your decision..."
                                    rows={4}
                                    maxLength={1000}
                                />
                                <span className="text-xs text-muted-foreground">
                                    {adminNotes.length}/1000 characters
                                </span>
                            </div>
                        </div>
                    )}

                    <DialogFooter>
                        <Button
                            variant="outline"
                            onClick={() => setIsReviewDialogOpen(false)}
                            className="cursor-pointer"
                        >
                            Cancel
                        </Button>
                        <Button
                            onClick={handleUpdateReport}
                            disabled={isUpdating || !newStatus}
                            className="cursor-pointer bg-uo-green text-white hover:bg-uo-green/90"
                        >
                            {isUpdating ? "Updating..." : "Update Report"}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}

