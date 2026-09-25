import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Fingerprint, Delete, Lock, Heart, X, Sparkles, Play, ShieldCheck, LockKeyhole } from "lucide-react";
import { Page } from "@/components/BottomNav";

export const Route = createFileRoute("/boveda")({
  head: () => ({
    meta: [
      { title: "Bóveda Íntima — Espacio privado" },
      { name: "description", content: "Espacio protegido con PIN: Desire Match y recuerdos íntimos cifrados." },
      { property: "og:title", content: "Bóveda Íntima — Espacio privado" },
      { property: "og:description", content: "Espacio protegido con PIN: Desire Match y recuerdos íntimos cifrados." },
    ],
  }),
  component: Boveda,
});

const PIN = "1402";

function Boveda() {
  const [unlocked, setUnlocked] = useState(false);
  return unlocked ? <Vault onLock={() => setUnlocked(false)} /> : <Unlock onOk={() => setUnlocked(true)} />;
}

function Unlock({ onOk }: { onOk: () => void }) {
  const [pin, setPin] = useState("");
  const [err, setErr] = useState(false);
  const [scan, setScan] = useState(false);
  const press = (d: string) => {
    if (pin.length >= 4) return;
    const n = pin + d;
    setPin(n);
    setErr(false);
    if (n.length === 4) setTimeout(() => (n === PIN ? onOk() : (setErr(true), setPin(""))), 250);
  };
  const bio = () => { setScan(true); setTimeout(onOk, 1300); };
  return (
    <main className="mx-auto flex min-h-screen max-w-md flex-col items-center px-6 pb-28 pt-14">
      <div className="grid h-16 w-16 place-items-center rounded-full bg-rose shadow-glow"><Lock className="h-7 w-7 text-primary-foreground" /></div>
      <h1 className="mt-4 text-4xl font-semibold text-rose">Bóveda Íntima</h1>
      <p className="mt-1 text-sm text-muted-foreground">Introduce vuestro PIN (demo: 1402)</p>
      <div className={`my-8 flex gap-4 ${err ? "animate-in shake" : ""}`}>
        {[0, 1, 2, 3].map((i) => (
          <span key={i} className={`h-3.5 w-3.5 rounded-full border border-primary ${i < pin.length ? "bg-primary" : ""} ${err ? "border-destructive" : ""}`} />
        ))}
      </div>
      {err && <p className="-mt-4 mb-4 text-xs text-destructive">PIN incorrecto</p>}
      <div className="grid w-full max-w-[260px] grid-cols-3 gap-4">
        {["1", "2", "3", "4", "5", "6", "7", "8", "9"].map((d) => (
          <button key={d} onClick={() => press(d)} className="glass aspect-square rounded-full font-display text-2xl active:bg-primary/20">{d}</button>
        ))}
        <button onClick={bio} aria-label="Biometría" className="grid aspect-square place-items-center rounded-full text-primary">
          <Fingerprint className={`h-8 w-8 ${scan ? "animate-pulse" : ""}`} />
        </button>
        <button onClick={() => press("0")} className="glass aspect-square rounded-full font-display text-2xl">0</button>
        <button onClick={() => setPin(pin.slice(0, -1))} aria-label="Borrar" className="grid aspect-square place-items-center rounded-full text-muted-foreground"><Delete className="h-6 w-6" /></button>
      </div>
      {scan && <p className="mt-6 text-sm text-primary">Verificando huella…</p>}
    </main>
  );
}

const cards = [
  { t: "Baño a la luz de las velas", partner: true },
  { t: "Fin de semana sin móviles", partner: true },
  { t: "Carta erótica escrita a mano", partner: false },
  { t: "Masaje con aceites al reencontrarnos", partner: true },
  { t: "Desayuno en la cama… y quedarnos", partner: true },
];

function Vault({ onLock }: { onLock: () => void }) {
  const [tab, setTab] = useState<"match" | "voz">("match");
  return (
    <Page title="Bóveda" subtitle="Solo vosotros dos">
      <button onClick={onLock} className="absolute right-4 top-8 flex items-center gap-1 rounded-full bg-muted px-3 py-1.5 text-xs text-muted-foreground" style={{ right: "max(1rem, calc(50% - 14rem + 1rem))" }}>
        <LockKeyhole className="h-3.5 w-3.5" />Bloquear
      </button>
      <div className="glass mb-5 grid grid-cols-2 rounded-full p-1">
        <button onClick={() => setTab("match")} className={`rounded-full py-2 text-sm ${tab === "match" ? "bg-rose text-primary-foreground" : "text-muted-foreground"}`}>Desire Match</button>
        <button onClick={() => setTab("voz")} className={`rounded-full py-2 text-sm ${tab === "voz" ? "bg-rose text-primary-foreground" : "text-muted-foreground"}`}>Recuerdos</button>
      </div>
      {tab === "match" ? <DesireMatch /> : <VoiceVault />}
    </Page>
  );
}

function DesireMatch() {
  const [i, setI] = useState(0);
  const [matches, setMatches] = useState<string[]>([]);
  const [flash, setFlash] = useState<string | null>(null);
  const [drag, setDrag] = useState(0);
  const [startX, setStartX] = useState<number | null>(null);

  const decide = (yes: boolean) => {
    const c = cards[i];
    if (!c) return;
    if (yes && c.partner) { setMatches((m) => [...m, c.t]); setFlash(c.t); setTimeout(() => setFlash(null), 1600); }
    setI(i + 1);
    setDrag(0);
  };

  const c = cards[i];
  return (
    <div>
      <p className="mb-4 text-center text-xs text-muted-foreground">Doble ciega: solo se revela si ambos decís que sí.</p>
      <div className="relative h-80">
        {c ? (
          <div
            onPointerDown={(e) => setStartX(e.clientX)}
            onPointerMove={(e) => startX !== null && setDrag(e.clientX - startX)}
            onPointerUp={() => { setStartX(null); if (Math.abs(drag) > 90) decide(drag > 0); else setDrag(0); }}
            className="glass absolute inset-0 flex touch-none select-none flex-col items-center justify-center rounded-3xl p-8 text-center shadow-glow"
            style={{ transform: `translateX(${drag}px) rotate(${drag / 18}deg)`, transition: startX === null ? "transform .3s" : "none" }}
          >
            <Sparkles className="mb-4 h-7 w-7 text-primary" />
            <p className="font-display text-3xl leading-tight">{c.t}</p>
            <p className="mt-6 text-xs text-muted-foreground">Desliza → sí · ← no</p>
            {drag > 40 && <span className="absolute left-5 top-5 rounded-full border border-success px-3 py-1 text-xs text-success">SÍ</span>}
            {drag < -40 && <span className="absolute right-5 top-5 rounded-full border border-destructive px-3 py-1 text-xs text-destructive">NO</span>}
          </div>
        ) : (
          <div className="glass absolute inset-0 grid place-items-center rounded-3xl p-8 text-center text-sm text-muted-foreground">Has visto todas las cartas de hoy ✨</div>
        )}
        {flash && (
          <div className="absolute inset-0 z-10 grid place-items-center rounded-3xl bg-background/80 backdrop-blur animate-in fade-in zoom-in-95">
            <div className="text-center"><Heart className="mx-auto h-12 w-12 fill-accent text-accent animate-beat" /><p className="mt-2 font-display text-3xl text-rose">¡Es un match!</p></div>
          </div>
        )}
      </div>
      {c && (
        <div className="mt-5 flex justify-center gap-6">
          <button onClick={() => decide(false)} className="glass grid h-14 w-14 place-items-center rounded-full text-muted-foreground"><X className="h-6 w-6" /></button>
          <button onClick={() => decide(true)} className="grid h-14 w-14 place-items-center rounded-full bg-rose text-primary-foreground shadow-glow"><Heart className="h-6 w-6" /></button>
        </div>
      )}
      <h2 className="mb-2 mt-8 text-2xl font-semibold">Vuestros matches</h2>
      {matches.length === 0 ? <p className="text-sm text-muted-foreground">Aún no hay coincidencias reveladas.</p> : (
        <ul className="space-y-2">{matches.map((m) => <li key={m} className="glass rounded-2xl px-4 py-3 text-sm">💞 {m}</li>)}</ul>
      )}
    </div>
  );
}

function VoiceVault() {
  const items = [
    { t: "Buenas noches, mi amor", by: "Yuki", d: "0:48" },
    { t: "Lo que no te dije en el aeropuerto", by: "Alberto", d: "2:12" },
    { t: "Nuestro primer aniversario", by: "Yuki", d: "1:05" },
  ];
  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2 rounded-2xl bg-muted px-4 py-3 text-xs text-muted-foreground"><ShieldCheck className="h-4 w-4 text-success" />Cifrado de extremo a extremo · solo vuestros dispositivos</div>
      {items.map((v) => (
        <div key={v.t} className="glass flex items-center gap-3 rounded-2xl p-3">
          <button className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-rose text-primary-foreground"><Play className="h-4 w-4" /></button>
          <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{v.t}</p><p className="text-[11px] text-muted-foreground">{v.by} · {v.d}</p></div>
          <Lock className="h-4 w-4 text-primary" />
        </div>
      ))}
      <div className="grid grid-cols-3 gap-2 pt-2">
        {[0, 1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="grid aspect-square place-items-center rounded-2xl bg-muted"><Lock className="h-5 w-5 text-muted-foreground" /></div>
        ))}
      </div>
    </div>
  );
}
