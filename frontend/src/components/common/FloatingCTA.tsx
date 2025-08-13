import React from "react";
import { Link } from "react-router-dom";

type Props =
  | {
      label: string;
      to: string;
      onClick?: never;
      ariaLabel?: string;
      variant?: "black" | "primary";
      className?: string;
    }
  | {
      label: string;
      onClick: () => void;
      to?: never;
      ariaLabel?: string;
      variant?: "black" | "primary";
      className?: string;
    };

const baseBtn =
  "pointer-events-auto block w-full text-center rounded-xl py-3 text-sm font-medium focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500";

const variantClass: Record<NonNullable<Props["variant"]>, string> = {
  black: "text-white bg-black",
  primary: "text-primary-foreground bg-primary",
};

const FloatingCTA: React.FC<Props> = ({
  label,
  to,
  onClick,
  ariaLabel,
  variant = "black",
  className,
}) => {
  return (
    <div className="fixed inset-x-0 bottom-0 z-40 pointer-events-none">
      <div className="max-w-md mx-auto px-4 pb-[max(20px,env(safe-area-inset-bottom))]">
        {to ? (
          <Link
            to={to}
            aria-label={ariaLabel || label}
            className={[baseBtn, variantClass[variant], className].join(" ")}
          >
            {label}
          </Link>
        ) : (
          <button
            type="button"
            aria-label={ariaLabel || label}
            onClick={onClick}
            className={[baseBtn, variantClass[variant], className].join(" ")}
          >
            {label}
          </button>
        )}
      </div>
    </div>
  );
};

export default FloatingCTA;


