"use client";

//Next
import Link from "next/link";
import { useTheme } from "next-themes";

//React and Other
import { useEffect, useState } from "react";
import {
    ArrowLeft,
    AlertCircle,
    FileText,
    Clock,
    CheckCircle,
    XCircle,
    Trash2,
} from "lucide-react";

// API
import api from "@/api/WebService";
import { ErrorResponse, ReportDTO, ReportStatus, ReportReason } from "@/api/WebTypes";

//ShadCN
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { Separator } from "@/components/ui/separator";

//Internal
import { MyAccountPopover } from "@/components/MyAccountPopover";

export default function ReportsPage() {
    const [outgoingReports, setOutgoingReports] = useState<ReportDTO[]>([]);
    const [incomingReports, setIncomingReports] = useState<ReportDTO[]>([]);
    const [mounted, setMounted] = useState(false);
    const [loadingOutgoing, setLoadingOutgoing] = useState(true);
    const [loadingIncoming, setLoadingIncoming] = useState(true);
    const { theme } = useTheme();

    useEffect(() => {
        setMounted(true);
    }, []);

    useEffect(() => {
        if (mounted) {
            loadReports();
        }
    }, [mounted]);

    async function loadReports() {
        // Load outgoing reports
        setLoadingOutgoing(true);
        const outgoingRes = await api.GetMyOutgoingReports(0, 50);
        setLoadingOutgoing(false);
        if (outgoingRes instanceof ErrorResponse) {
            toast.error("Failed to load outgoing reports: " + outgoingRes.body?.error);
        } else {
            setOutgoingReports(outgoingRes.content);
        }

        // Load incoming reports
        setLoadingIncoming(true);
        const incomingRes = await api.GetMyIncomingReports(0, 50);
        setLoadingIncoming(false);
        if (incomingRes instanceof ErrorResponse) {
            toast.error("Failed to load incoming reports: " + incomingRes.body?.error);
        } else {
            setIncomingReports(incomingRes.content);
        }
    }

    if (!mounted) return null;

    return (
        <div className="flex h-screen bg-background transition-colors duration-300">
            {/* Sidebar */}
            <aside className={`w-64 border-r bg-muted/10 p-4 flex flex-col transition-colors duration-300 ${theme !== "dark" && "shadow-[2px_0_10px_rgba(0,0,0,0.15)]"}`}>
                <Button variant={'link'} style={{ color: 'gray', justifyContent: 'flex-start' }} className="!p-0 !px-0 !py-0 hover:underline hover:bg-none cursor-pointer mb-4">
                    <Link style={{ flexDirection: 'row' }} className="flex items-center gap-1" href="/dashboard">
                        <ArrowLeft />
                        back to dashboard
                    </Link>
                </Button>

                {/* Top section */}
                <div className="flex items-center gap-2 mb-6 justify-between">
                    <h2 className="text-xl font-semibold">My Reports</h2>
                </div>

                {/* Account */}
                <div className="mb-4">
                    <MyAccountPopover />
                </div>

                {/* Stats */}
                <div className="space-y-2">
                    <div className="text-md text-muted-foreground">
                        <p>Outgoing: <span className="font-semibold text-foreground">{outgoingReports.length}</span></p>
                        <p>Incoming: <span className="font-semibold text-foreground">{incomingReports.length}</span></p>
                    </div>
                </div>
            </aside>

            {/* Main content */}
            <main className="flex-1 overflow-y-auto p-6 transition-colors duration-300">
                <div className="max-w-6xl mx-auto space-y-12">
                    {/* Outgoing Reports Section */}
                    <section>
                        <div className="flex items-center gap-3 mb-3">
                            <FileText className="h-7 w-7" />
                            <h2 className="text-3xl font-bold">Reports You've Made</h2>
                        </div>
                        <p className="text-muted-foreground mb-6 text-base">
                            These are reports you've filed against other listings.
                        </p>
                        
                        {loadingOutgoing ? (
                            <div className="space-y-4">
                                {Array.from({ length: 3 }).map((_, i) => (
                                    <Card key={i} className="transition-colors duration-300">
                                        <CardContent className="p-6">
                                            <Skeleton className="h-6 w-3/4 mb-2" />
                                            <Skeleton className="h-4 w-1/2 mb-2" />
                                            <Skeleton className="h-4 w-2/3" />
                                        </CardContent>
                                    </Card>
                                ))}
                            </div>
                        ) : outgoingReports.length === 0 ? (
                            <Card>
                                <CardContent className="p-8 text-center text-muted-foreground">
                                    <AlertCircle className="h-12 w-12 mx-auto mb-4 opacity-50" />
                                    <p>You haven't made any reports yet.</p>
                                </CardContent>
                            </Card>
                        ) : (
                            <div className="space-y-4">
                                {outgoingReports.map((report) => (
                                    <ReportCard key={report.reportGU} report={report} type="outgoing" />
                                ))}
                            </div>
                        )}
                    </section>

                    <Separator className="my-12" />

                    {/* Incoming Reports Section */}
                    <section>
                        <div className="flex items-center gap-3 mb-3">
                            <AlertCircle className="h-7 w-7 text-orange-500" />
                            <h2 className="text-3xl font-bold">Reports Against Your Listings</h2>
                        </div>
                        <p className="text-muted-foreground mb-6 text-base">
                            These are reports that others have filed against your listings.
                        </p>
                        
                        {loadingIncoming ? (
                            <div className="space-y-4">
                                {Array.from({ length: 3 }).map((_, i) => (
                                    <Card key={i} className="transition-colors duration-300">
                                        <CardContent className="p-6">
                                            <Skeleton className="h-6 w-3/4 mb-2" />
                                            <Skeleton className="h-4 w-1/2 mb-2" />
                                            <Skeleton className="h-4 w-2/3" />
                                        </CardContent>
                                    </Card>
                                ))}
                            </div>
                        ) : incomingReports.length === 0 ? (
                            <Card>
                                <CardContent className="p-8 text-center text-muted-foreground">
                                    <CheckCircle className="h-12 w-12 mx-auto mb-4 opacity-50 text-green-500" />
                                    <p>No reports have been filed against your listings.</p>
                                </CardContent>
                            </Card>
                        ) : (
                            <div className="space-y-4">
                                {incomingReports.map((report) => (
                                    <ReportCard key={report.reportGU} report={report} type="incoming" />
                                ))}
                            </div>
                        )}
                    </section>
                </div>
            </main>
        </div>
    );
}

function ReportCard({ report, type }: { report: ReportDTO; type: "outgoing" | "incoming" }) {
    const { theme } = useTheme();

    const getStatusIcon = (status: ReportStatus) => {
        switch (status) {
            case ReportStatus.PENDING:
                return <Clock className="h-5 w-5 text-yellow-500" />;
            case ReportStatus.UNDER_REVIEW:
                return <FileText className="h-5 w-5 text-blue-500" />;
            case ReportStatus.RESOLVED:
                return <CheckCircle className="h-5 w-5 text-green-500" />;
            case ReportStatus.DISMISSED:
                return <XCircle className="h-5 w-5 text-gray-500" />;
            case ReportStatus.LISTING_REMOVED:
                return <Trash2 className="h-5 w-5 text-red-500" />;
            default:
                return <AlertCircle className="h-5 w-5" />;
        }
    };

    const getStatusColor = (status: ReportStatus) => {
        switch (status) {
            case ReportStatus.PENDING:
                return "text-yellow-600 bg-yellow-100 dark:bg-yellow-900/20";
            case ReportStatus.UNDER_REVIEW:
                return "text-blue-600 bg-blue-100 dark:bg-blue-900/20";
            case ReportStatus.RESOLVED:
                return "text-green-600 bg-green-100 dark:bg-green-900/20";
            case ReportStatus.DISMISSED:
                return "text-gray-600 bg-gray-100 dark:bg-gray-900/20";
            case ReportStatus.LISTING_REMOVED:
                return "text-red-600 bg-red-100 dark:bg-red-900/20";
            default:
                return "text-gray-600 bg-gray-100";
        }
    };

    const formatReason = (reason: ReportReason) => {
        return reason.split('_').map(word => 
            word.charAt(0) + word.slice(1).toLowerCase()
        ).join(' ');
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            month: 'short', 
            day: 'numeric', 
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <Card className={`${theme !== 'dark' && 'hover:shadow-md'} transition-all duration-300`}>
            <CardContent className="p-6">
                <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                            <h3 className="font-semibold text-lg">
                                {type === "outgoing" ? "Against: " : "Your listing: "}
                                <Link 
                                    href={`/dashboard/listing/${report.listingGU}`}
                                    className="text-primary hover:underline"
                                >
                                    {report.listingTitle}
                                </Link>
                            </h3>
                        </div>
                        
                        {type === "incoming" && (
                            <p className="text-sm text-muted-foreground mb-2">
                                Reported by: <span className="font-medium">{report.username}</span>
                            </p>
                        )}
                        
                        <div className="flex flex-wrap items-center gap-2 mb-3">
                            <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(report.status)}`}>
                                {getStatusIcon(report.status)}
                                {report.status.replace(/_/g, ' ')}
                            </span>
                            <span className="text-xs px-3 py-1 rounded-full bg-muted text-muted-foreground">
                                {formatReason(report.reason)}
                            </span>
                        </div>
                        
                        {report.message && (
                            <p className="text-sm text-muted-foreground mb-2">
                                <span className="font-medium">Details:</span> {report.message}
                            </p>
                        )}
                        
                        {report.adminNotes && (
                            <div className="mt-3 p-3 bg-muted/50 rounded-md">
                                <p className="text-sm font-medium mb-1">Admin Notes:</p>
                                <p className="text-sm text-muted-foreground">{report.adminNotes}</p>
                            </div>
                        )}
                    </div>
                </div>
                
                <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>Reported: {formatDate(report.createdAt)}</span>
                    {report.reviewedAt && (
                        <span>Reviewed: {formatDate(report.reviewedAt)}</span>
                    )}
                </div>
            </CardContent>
        </Card>
    );
}

