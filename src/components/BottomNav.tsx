import { Link } from "@tanstack/react-router";
import { Home, MessageCircleHeart, Clapperboard, Images, LockKeyhole } from "lucide-react";

const items = [
  { to: "/", label: "Hogar", icon: Home },
  { to: "/conexion", label: "Conexión", icon: MessageCircleHeart },
  { to: "/cine", label: "Cine", icon: Clapperboard },
  { to: "/feed", label: "Feed", icon: Images },
  { to: "/boveda", label: "Bóveda", icon: LockKeyhole },
] as const;

export function BottomNav() {
  return (
    <nav className="fixed inset-x-0 bottom-0 z-40 mx-auto max-w-md px-3 pb-3">
      <div className="glass grid grid-cols-5 rounded-3xl px-1 py-2">
        {items.map(({ to, label, icon: Icon }) => (
          <Link
            key={to}
            to={to}
            activeOptions={{ exact: true }}
            className="group flex flex-col items-center gap-1 rounded-2xl py-1.5 text-muted-foreground transition-colors"
            activeProps={{ className: "text-primary" }}
          >
            <Icon className="h-5 w-5" strokeWidth={1.7} />
            <span className="text-[10px] font-medium tracking-wide">{label}</span>
          </Link>
        ))}
      </div>
    </nav>
  );
}

export function Page({ title, subtitle, children }: { title: string; subtitle?: string; children: React.ReactNode }) {
  return (
    <main className="mx-auto min-h-screen max-w-md px-4 pb-28 pt-6">
      <header className="mb-5">
        {subtitle && <p className="text-xs uppercase tracking-[0.25em] text-muted-foreground">{subtitle}</p>}
        <h1 className="text-4xl font-semibold text-rose">{title}</h1>
      </header>
      {children}
    </main>
  );
}
