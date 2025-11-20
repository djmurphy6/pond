import { Menu } from "lucide-react";
import ThemeToggle from "./ThemeToggle";

export default function MobileHeader(props: { onPress: React.Dispatch<React.SetStateAction<boolean>> }) {
    const { onPress } = props;
    return (
        <div className="flex md:hidden items-center justify-between p-4 border-b bg-muted/70 sticky top-0 z-20">
            <button onClick={() => onPress(true)} className="flex items-center gap-2">
                <Menu className="h-8 w-8" />
            </button>
            <span className="font-bold text-2xl">Pond</span>
            <ThemeToggle />
        </div>
    )
}