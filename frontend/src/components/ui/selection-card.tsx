import { cn } from "@/utils/cn";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { CheckCircle2 } from "lucide-react";
import { ReactNode } from "react";

interface SelectionCardProps {
  title?: string;
  description?: string;
  selected?: boolean;
  onClick?: () => void;
  className?: string;
  children?: ReactNode;
  showCheckIcon?: boolean;
}

export const SelectionCard: React.FC<SelectionCardProps> = ({
  title,
  description,
  selected = false,
  onClick,
  className,
  children,
  showCheckIcon = true,
}) => {
  return (
    <Card
      role="button"
      tabIndex={0}
      onClick={onClick}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          onClick?.();
        }
      }}
      className={cn(
        "relative cursor-pointer border transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary",
        selected ? "border-primary shadow-sm bg-primary/5" : "bg-slate-50",
        className
      )}
    >
      {children ? (
        children
      ) : (
        <CardHeader className="pr-12">
          <CardTitle className="text-base font-semibold text-foreground ">
            {title}
          </CardTitle>
          {description ? (
            <CardDescription className="text-sm text-muted-foreground">
              {description}
            </CardDescription>
          ) : null}
        </CardHeader>
      )}
      {showCheckIcon && (
        <CheckCircle2
          className={cn(
            "absolute right-4 h-6 w-6 top-1/2 -translate-y-1/2",
            selected ? "text-primary" : "text-muted-foreground/30"
          )}
          aria-hidden
        />
      )}
    </Card>
  );
};
