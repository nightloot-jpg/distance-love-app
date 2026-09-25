import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useRef, useState } from "react";
import { Play, Pause, Mic, MicOff, Send, Link2, RefreshCw } from "lucide-react";
import { Page } from "@/components/BottomNav";

export const Route = createFileRoute("/cine")({
  head: () => ({
    meta: [
      { title: "Sala de Cine — Ver juntos" },
      {
        name: "description",
        content: "Mirad vídeos sincronizados con reacciones y chat en directo.",
      },
      { property: "og:title", content: "Sala de Cine — Ver juntos" },
      {
        property: "og:description",
        content: "Mirad vídeos sincronizados con reacciones y chat en directo.",
      },
    ],
  }),
  component: Cine,
});

const library = [
  {
    title: "Big Buck Bunny",
    url: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
  },
  {
    title: "Sintel",
    url: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
  },
  {
    title: "Tears of Steel",
    url: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
  },
];

function ytId(url: string) {
  const m = url.match(/(?:youtu\.be\/|v=|embed\/|shorts\/)([\w-]{11})/);
  return m?.[1] ?? null;
}

const t = (s: number) => `${Math.floor(s / 60)}:${String(Math.floor(s % 60)).padStart(2, "0")}`;

function Cine() {
  const [src, setSrc] = useState(library[0]!.url);
  const [link, setLink] = useState("");
  const [playing, setPlaying] = useState(false);
  const [time, setTime] = useState(0);
  const [dur, setDur] = useState(0);
  const [floaters, setFloaters] = useState<{ id: number; e: string; x: number }[]>([]);
  const [mic, setMic] = useState(false);
  const [chat, setChat] = useState([
    { by: "Yuki", text: "¡Lista! Dale al play cuando quieras 🍿" },
  ]);
  const [msg, setMsg] = useState("");
  const vid = useRef<HTMLVideoElement>(null);
  const yt = ytId(src);

  useEffect(() => {
    setPlaying(false);
    setTime(0);
  }, [src]);

  const toggle = () => {
    const v = vid.current;
    if (!v) return;
    if (v.paused) {
      v.play();
      setPlaying(true);
    } else {
      v.pause();
      setPlaying(false);
    }
  };
  const react = (e: string) => {
    const id = Date.now() + Math.random();
    setFloaters((f) => [...f, { id, e, x: 10 + Math.random() * 80 }]);
    setTimeout(() => setFloaters((f) => f.filter((x) => x.id !== id)), 2400);
  };
  const send = () => {
    if (!msg.trim()) return;
    setChat([...chat, { by: "Tú", text: msg }]);
    setMsg("");
    setTimeout(() => {
      setChat((c) => [...c, { by: "Yuki", text: "💕" }]);
      react("❤️");
    }, 1200);
  };

  return (
    <Page title="Sala de Cine" subtitle="En directo con Yuki">
      <div className="relative overflow-hidden rounded-3xl bg-card shadow-glow">
        {yt ? (
          <iframe
            className="aspect-video w-full"
            src={`https://www.youtube.com/embed/${yt}`}
            allow="autoplay; encrypted-media"
            allowFullScreen
            title="YouTube"
          />
        ) : (
          <video
            ref={vid}
            src={src}
            className="aspect-video w-full bg-background"
            playsInline
            onClick={toggle}
            onTimeUpdate={(e) => setTime(e.currentTarget.currentTime)}
            onLoadedMetadata={(e) => setDur(e.currentTarget.duration)}
          />
        )}
        <div className="pointer-events-none absolute inset-0">
          {floaters.map((f) => (
            <span
              key={f.id}
              className="absolute bottom-4 text-3xl animate-float-up"
              style={{ left: `${f.x}%` }}
            >
              {f.e}
            </span>
          ))}
        </div>
        <div className="absolute left-3 top-3 flex items-center gap-1.5 rounded-full bg-background/70 px-2.5 py-1 text-[10px] backdrop-blur">
          <RefreshCw className="h-3 w-3 text-success" />
          Sincronizado · 2 viendo
        </div>
      </div>

      {!yt && (
        <div className="glass mt-3 flex items-center gap-3 rounded-2xl px-3 py-2">
          <button
            onClick={toggle}
            className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-rose text-primary-foreground"
          >
            {playing ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
          </button>
          <span className="text-[11px] tabular-nums text-muted-foreground">{t(time)}</span>
          <input
            type="range"
            min={0}
            max={dur || 0}
            step={0.1}
            value={time}
            onChange={(e) => {
              const v = Number(e.target.value);
              if (vid.current) vid.current.currentTime = v;
              setTime(v);
            }}
            className="min-w-0 flex-1 accent-[var(--primary)]"
          />
          <span className="text-[11px] tabular-nums text-muted-foreground">{t(dur)}</span>
        </div>
      )}

      <div className="mt-3 flex items-center justify-between gap-2">
        <div className="glass flex gap-1 rounded-full px-2 py-1">
          {["❤️", "🍿", "😂", "😭", "😍", "🔥"].map((e) => (
            <button
              key={e}
              onClick={() => react(e)}
              className="rounded-full px-1.5 py-1 text-xl transition active:scale-125"
            >
              {e}
            </button>
          ))}
        </div>
        <button
          onClick={() => setMic(!mic)}
          className={`grid h-11 w-11 shrink-0 place-items-center rounded-full ${mic ? "bg-rose text-primary-foreground shadow-glow" : "glass text-muted-foreground"}`}
          aria-label="Voz"
        >
          {mic ? <Mic className="h-5 w-5" /> : <MicOff className="h-5 w-5" />}
        </button>
      </div>
      {mic && (
        <p className="mt-2 text-center text-xs text-primary">🎙️ Canal de voz abierto con Yuki</p>
      )}

      <div className="glass mt-4 rounded-3xl p-3">
        <div className="mb-3 flex gap-2 overflow-x-auto">
          {library.map((v) => (
            <button
              key={v.url}
              onClick={() => setSrc(v.url)}
              className={`shrink-0 rounded-full px-3 py-1.5 text-xs ${src === v.url ? "bg-rose text-primary-foreground" : "bg-muted text-muted-foreground"}`}
            >
              {v.title}
            </button>
          ))}
        </div>
        <div className="flex gap-2">
          <div className="flex min-w-0 flex-1 items-center gap-2 rounded-full bg-muted px-3">
            <Link2 className="h-4 w-4 shrink-0 text-muted-foreground" />
            <input
              value={link}
              onChange={(e) => setLink(e.target.value)}
              placeholder="Enlace de YouTube o .mp4"
              className="min-w-0 flex-1 bg-transparent py-2 text-sm outline-none"
            />
          </div>
          <button
            onClick={() => {
              if (link.trim()) {
                setSrc(link.trim());
                setLink("");
              }
            }}
            className="shrink-0 rounded-full bg-rose px-4 text-sm font-semibold text-primary-foreground"
          >
            Cargar
          </button>
        </div>
      </div>

      <div className="glass mt-4 rounded-3xl p-3">
        <p className="mb-2 text-xs text-muted-foreground">Chat de sala</p>
        <div className="mb-3 max-h-48 space-y-2 overflow-y-auto">
          {chat.map((c, i) => (
            <div
              key={i}
              className={`max-w-[80%] rounded-2xl px-3 py-2 text-sm ${c.by === "Tú" ? "ml-auto bg-rose text-primary-foreground" : "bg-muted"}`}
            >
              {c.text}
            </div>
          ))}
        </div>
        <div className="flex gap-2">
          <input
            value={msg}
            onChange={(e) => setMsg(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && send()}
            placeholder="Escribe…"
            className="min-w-0 flex-1 rounded-full bg-muted px-4 py-2 text-sm outline-none"
          />
          <button
            onClick={send}
            className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-rose text-primary-foreground"
          >
            <Send className="h-4 w-4" />
          </button>
        </div>
      </div>
    </Page>
  );
}
