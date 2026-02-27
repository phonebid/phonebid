"use client";

import { cn } from "@/utils/cn";
import React, { ReactNode } from "react";

interface AuroraBackgroundProps extends React.HTMLProps<HTMLDivElement> {
  children: ReactNode;
  showRadialGradient?: boolean;
}

export const AuroraBackground = ({
  className,
  children,
  showRadialGradient = true,
  ...props
}: AuroraBackgroundProps) => {
  return (
    <main>
      <div
        className={cn(
          "relative flex flex-col h-[100vh] items-center justify-center bg-zinc-50 dark:bg-zinc-900 text-slate-950 transition-bg",
          className
        )}
        {...props}
      >
        <div className="absolute inset-0 overflow-hidden [contain:paint]">
          <div
            className={cn(
              `
            [--white-gradient:repeating-linear-gradient(100deg,var(--white)_0%,var(--white)_7%,var(--transparent)_10%,var(--transparent)_12%,var(--white)_16%)]
            [--dark-gradient:repeating-linear-gradient(100deg,var(--black)_0%,var(--black)_7%,var(--transparent)_10%,var(--transparent)_12%,var(--black)_16%)]
            [--aurora:repeating-linear-gradient(100deg,var(--violet-500)_10%,var(--purple-500)_15%,var(--violet-400)_20%,var(--indigo-400)_25%,var(--cyan-400)_30%)]
            [background-image:var(--white-gradient),var(--aurora)]
            dark:[background-image:var(--dark-gradient),var(--aurora)]
            [background-size:300%,_200%]
            [background-position:50%_50%,50%_50%]
            invert dark:invert-0
            pointer-events-none
            absolute -inset-[10px] opacity-50`,
              showRadialGradient &&
                `[mask-image:radial-gradient(ellipse_at_100%_0%,black_10%,var(--transparent)_70%)]`
            )}
          />
          <div
            className={cn(
              `
            absolute inset-0 overflow-hidden mix-blend-difference pointer-events-none
            [--white-gradient:repeating-linear-gradient(100deg,var(--white)_0%,var(--white)_7%,var(--transparent)_10%,var(--transparent)_12%,var(--white)_16%)]
            [--dark-gradient:repeating-linear-gradient(100deg,var(--black)_0%,var(--black)_7%,var(--transparent)_10%,var(--transparent)_12%,var(--black)_16%)]
            [--aurora:repeating-linear-gradient(100deg,var(--violet-500)_10%,var(--purple-500)_15%,var(--violet-400)_20%,var(--indigo-400)_25%,var(--cyan-400)_30%)]
            opacity-50`,
              showRadialGradient &&
                "[mask-image:radial-gradient(ellipse_at_100%_0%,black_10%,var(--transparent)_70%)]"
            )}
          >
            <div
              className="absolute inset-0 w-[400%] h-full will-change-transform animate-aurora-slide
                [background-image:var(--white-gradient),var(--aurora)]
                dark:[background-image:var(--dark-gradient),var(--aurora)]
                [background-size:200%,_100%]
                [background-position:0_0,0_0]"
            />
          </div>
        </div>
        {children}
      </div>
    </main>
  );
};
