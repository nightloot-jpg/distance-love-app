import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useRef, useState } from "react";
import { BatteryMedium, BatteryLow, Sun, CloudRain, Heart, Plus, X, Check } from "lucide-react";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Espacio Compartido — Nosotros" },
      { name: "description", content: "Vuestras horas, el contador del reencuentro y un latido en directo." },
      { property: "og:title", content: "Espacio Compartido — Nosotros" },
      { property: "og:description", content: "Vuestras horas, el contador del reencuentro y un latido en directo." },
    ],
  }),
  component: Hub,
});

const REUNION = new Date("2026-10-18T10:00:00");

function useNow() {
  const [now, setNow] = useState<Date | null>(null);
  useEffect(() => {
    setNow(new Date());
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);
  return now;
}

function fmt(now: Date | null, tz: string) {
  if (!now) return "--:--";
  return now.toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit", timeZone: tz });
}

function Hub() {
  const now = useNow();
  const people = [
    { name: "Alberto", city: "Madrid", tz: "Europe/Madrid", temp: "22°", Weather: Sun, battery: 78, Bat: BatteryMedium, status: "Libre", dot: "bg-success" },
    { name: "Yuki", city: "Tokio", tz: "Asia/Tokyo", temp: "19°", Weather: CloudRain, battery: 23, Bat: BatteryLow, status: "Durmiendo", dot: "bg-muted-foreground" },
  ];
  const diff = now ? Math.max(0, REUNION.getTime() - now.getTime()) : 0;
  const d = Math.floor(diff / 864e5), h = Math.floor(diff / 36e5) % 24, m = Math.floor(diff / 6e4) % 60, s = Math.floor(diff / 1e3) % 60;

  return (
    <main className="mx-auto min-h-screen max-w-md px-4 pb-28 pt-6">
      <p className="text-xs uppercase tracking-[0.25em] text-muted-foreground">Espacio compartido</p>
      <h1 className="mb-5 text-4xl font-semibold text-rose">Nosotros</h1>

      <section className="grid grid-cols-2 gap-3">
        {people.map((p) => (
          <div key={p.name} className="glass rounded-3xl p-4">
            <div className="flex items-center gap-2">
              <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-rose font-display text-lg font-bold text-primary-foreground">{p.name[0]}</div>
              <div className="min-w-0">
                <p className="truncate text-sm font-semibold">{p.name}</p>
                <p className="flex items-center gap-1 text-[11px] text-muted-foreground"><span className={`h-1.5 w-1.5 rounded-full ${p.dot}`} />{p.status}</p>
              </div>
            </div>
            <p className="mt-3 font-display text-3xl font-semibold tabular-nums">{fmt(now, p.tz)}</p>
            <p className="text-xs text-muted-foreground">{p.city}</p>
            <div className="mt-2 flex items-center justify-between text-xs text-muted-foreground">
              <span className="flex items-center gap-1"><p.Weather className="h-3.5 w-3.5 text-primary" />{p.temp}</span>
              <span className="flex items-center gap-1"><p.Bat className="h-3.5 w-3.5" />{p.battery}%</span>
            </div>
          </div>
        ))}
      </section>

      <section className="glass mt-4 rounded-3xl p-5 text-center">
        <p className="text-xs uppercase tracking-[0.2em] text-muted-foreground">Próximo reencuentro · 18 oct</p>
        <p className="mt-1 font-display text-6xl font-bold text-rose">{d}</p>
        <p className="text-sm text-muted-foreground">días para volver a vernos</p>
        <div className="mt-3 flex justify-center gap-4 text-xs tabular-nums text-muted-foreground">
          <span>{h}h</span><span>{m}m</span><span>{s}s</span>
        </div>
      </section>

      <Heartbeat />
      <Notes />
    </main>
  );
}

function Heartbeat() {
  const [holding, setHolding] = useState(false);
  const [rings, setRings] = useState<number[]>([]);
  const timer = useRef<ReturnType<typeof setInterval> | null>(null);

  const start = () => {
    setHolding(true);
    const pulse = () => {
      const id = Date.now() + Math.random();
      setRings((r) => [...r, id]);
      setTimeout(() => setRings((r) => r.filter((x) => x !== id)), 1600);
      if (typeof navigator !== "undefined" && "vibrate" in navigator) navigator.vibrate?.([40, 80, 40]);
    };
    pulse();
    timer.current = setInterval(pulse, 900);
  };
  const stop = () => {
    setHolding(false);
    if (timer.current) clearInterval(timer.current);
  };

  return (
    <section className="my-8 flex flex-col items-center">
      <div className="relative grid h-44 w-44 place-items-center">
        {rings.map((id) => (
          <span key={id} className="absolute h-24 w-24 rounded-full border-2 border-primary animate-ripple" />
        ))}
        <button
          onPointerDown={start}
          onPointerUp={stop}
          onPointerLeave={stop}
          onContextMenu={(e) => e.preventDefault()}
          aria-label="Enviar latido"
          className={`relative grid h-24 w-24 select-none place-items-center rounded-full bg-rose shadow-glow transition-transform ${holding ? "animate-beat" : ""}`}
        >
          <Heart className="h-10 w-10 fill-primary-foreground text-primary-foreground" />
        </button>
      </div>
      <p className="text-sm text-muted-foreground">{holding ? "Yuki siente tu latido…" : "Mantén pulsado para enviar tu latido"}</p>
    </section>
  );
}

function Notes() {
  const [notes, setNotes] = useState([
    { id: 1, text: "Videollamada el domingo a las 11h (mi hora) 💛", by: "Yuki", done: false },
    { id: 2, text: "Comprar billetes de tren Kioto", by: "Alberto", done: true },
    { id: 3, text: "Mándame la receta del ramen", by: "Alberto", done: false },
  ]);
  const [text, setText] = useState("");
  const add = () => {
    if (!text.trim()) return;
    setNotes([{ id: Date.now(), text, by: "Alberto", done: false }, ...notes]);
    setText("");
  };
  return (
    <section className="glass rounded-3xl p-4">
      <h2 className="mb-3 text-2xl font-semibold">Pizarrón</h2>
      <div className="mb-3 flex gap-2">
        <input value={text} onChange={(e) => setText(e.target.value)} onKeyDown={(e) => e.key === "Enter" && add()} placeholder="Nota o recordatorio…" className="min-w-0 flex-1 rounded-full bg-muted px-4 py-2 text-sm outline-none focus:ring-1 focus:ring-ring" />
        <button onClick={add} className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-rose text-primary-foreground"><Plus className="h-4 w-4" /></button>
      </div>
      <ul className="space-y-2">
        {notes.map((n) => (
          <li key={n.id} className="flex items-center gap-3 rounded-2xl bg-muted/60 px-3 py-2.5">
            <button onClick={() => setNotes(notes.map((x) => x.id === n.id ? { ...x, done: !x.done } : x))} className={`grid h-5 w-5 shrink-0 place-items-center rounded-full border border-primary ${n.done ? "bg-primary" : ""}`}>
              {n.done && <Check className="h-3 w-3 text-primary-foreground" />}
            </button>
            <div className="min-w-0 flex-1">
              <p className={`text-sm ${n.done ? "text-muted-foreground line-through" : ""}`}>{n.text}</p>
              <p className="text-[10px] text-muted-foreground">{n.by}</p>
            </div>
            <button onClick={() => setNotes(notes.filter((x) => x.id !== n.id))} className="text-muted-foreground"><X className="h-4 w-4" /></button>
          </li>
        ))}
      </ul>
    </section>
  );
}
