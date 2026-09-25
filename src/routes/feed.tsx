import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import {
  Heart,
  MessageCircle,
  MapPin,
  Play,
  Pause,
  LayoutGrid,
  Rows3,
  X,
  Send,
} from "lucide-react";
import { Page } from "@/components/BottomNav";
import f1 from "@/assets/feed1.jpg";
import f2 from "@/assets/feed2.jpg";
import f3 from "@/assets/feed3.jpg";

export const Route = createFileRoute("/feed")({
  head: () => ({
    meta: [
      { title: "Nuestro Feed — Recuerdos para dos" },
      {
        name: "description",
        content: "Un Instagram privado solo para vosotros: historias, fotos y notas de voz.",
      },
      { property: "og:title", content: "Nuestro Feed — Recuerdos para dos" },
      {
        property: "og:description",
        content: "Un Instagram privado solo para vosotros: historias, fotos y notas de voz.",
      },
    ],
  }),
  component: Feed,
});

type Post = {
  id: number;
  by: string;
  img: string;
  text: string;
  place: string;
  ago: string;
  voice?: number;
  liked: boolean;
  comments: { by: string; text: string }[];
};

const initial: Post[] = [
  {
    id: 1,
    by: "Yuki",
    img: f2,
    text: "Shibuya bajo la lluvia. Pensé en ti en cada paraguas.",
    place: "Shibuya, Tokio",
    ago: "hace 2 h",
    voice: 14,
    liked: true,
    comments: [{ by: "Alberto", text: "Quiero pasear ahí contigo 🌧️" }],
  },
  {
    id: 2,
    by: "Alberto",
    img: f3,
    text: "El atardecer de hoy te pertenece.",
    place: "La Latina, Madrid",
    ago: "ayer",
    liked: false,
    comments: [],
  },
  {
    id: 3,
    by: "Yuki",
    img: f1,
    text: "Nuestro café favorito, versión a distancia ☕",
    place: "Kioto",
    ago: "hace 3 días",
    voice: 22,
    liked: true,
    comments: [],
  },
];

function Feed() {
  const [posts, setPosts] = useState(initial);
  const [view, setView] = useState<"feed" | "grid">("feed");
  const [story, setStory] = useState<number | null>(null);
  const stories = [
    { name: "Tú", img: f3 },
    { name: "Yuki", img: f2 },
  ];

  return (
    <Page title="Nuestro Feed" subtitle="Solo para dos">
      <div className="mb-5 flex items-center gap-4">
        {stories.map((s, i) => (
          <button
            key={s.name}
            onClick={() => setStory(i)}
            className="flex flex-col items-center gap-1"
          >
            <div className="rounded-full bg-rose p-[2.5px] shadow-glow">
              <img
                src={s.img}
                alt={s.name}
                className="h-16 w-16 rounded-full border-2 border-background object-cover"
              />
            </div>
            <span className="text-xs text-muted-foreground">{s.name}</span>
          </button>
        ))}
        <div className="ml-auto glass flex rounded-full p-1">
          <button
            onClick={() => setView("feed")}
            aria-label="Feed"
            className={`rounded-full p-2 ${view === "feed" ? "bg-rose text-primary-foreground" : "text-muted-foreground"}`}
          >
            <Rows3 className="h-4 w-4" />
          </button>
          <button
            onClick={() => setView("grid")}
            aria-label="Cuadrícula"
            className={`rounded-full p-2 ${view === "grid" ? "bg-rose text-primary-foreground" : "text-muted-foreground"}`}
          >
            <LayoutGrid className="h-4 w-4" />
          </button>
        </div>
      </div>

      {view === "grid" ? (
        <div className="grid grid-cols-3 gap-1 overflow-hidden rounded-2xl">
          {Array.from({ length: 9 }).map((_, i) => {
            const p = posts[i % posts.length]!;
            return (
              <img
                key={i}
                src={p.img}
                alt=""
                className="aspect-square w-full object-cover"
                style={{ filter: `hue-rotate(${i * 8}deg)` }}
              />
            );
          })}
        </div>
      ) : (
        <div className="space-y-5">
          {posts.map((p) => (
            <PostCard
              key={p.id}
              p={p}
              onChange={(np) => setPosts(posts.map((x) => (x.id === np.id ? np : x)))}
            />
          ))}
        </div>
      )}

      {story !== null && <StoryViewer s={stories[story]!} onClose={() => setStory(null)} />}
    </Page>
  );
}

function PostCard({ p, onChange }: { p: Post; onChange: (p: Post) => void }) {
  const [open, setOpen] = useState(false);
  const [c, setC] = useState("");
  return (
    <article className="glass overflow-hidden rounded-3xl">
      <div className="flex items-center gap-2 p-3">
        <div className="grid h-8 w-8 place-items-center rounded-full bg-rose text-sm font-bold text-primary-foreground">
          {p.by[0]}
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-sm font-semibold">{p.by}</p>
          <p className="flex items-center gap-1 text-[11px] text-muted-foreground">
            <MapPin className="h-3 w-3" />
            {p.place}
          </p>
        </div>
        <span className="text-[11px] text-muted-foreground">{p.ago}</span>
      </div>
      <img
        src={p.img}
        alt={p.text}
        className="aspect-square w-full object-cover"
        onDoubleClick={() => onChange({ ...p, liked: true })}
      />
      <div className="space-y-3 p-3">
        {p.voice && <Voice secs={p.voice} />}
        <div className="flex items-center gap-4">
          <button
            onClick={() => onChange({ ...p, liked: !p.liked })}
            className="flex items-center gap-1.5 text-sm"
          >
            <Heart
              className={`h-5 w-5 transition ${p.liked ? "fill-accent text-accent scale-110" : ""}`}
            />
            <span className="text-muted-foreground">{p.liked ? "1/1 ❤️" : "0/1"}</span>
          </button>
          <button
            onClick={() => setOpen(!open)}
            className="flex items-center gap-1.5 text-sm text-muted-foreground"
          >
            <MessageCircle className="h-5 w-5" />
            {p.comments.length}
          </button>
        </div>
        <p className="text-sm">
          <span className="font-semibold">{p.by}</span> {p.text}
        </p>
        {open && (
          <div className="space-y-2">
            {p.comments.map((cm, i) => (
              <p key={i} className="text-sm">
                <span className="font-semibold">{cm.by}</span> {cm.text}
              </p>
            ))}
            <div className="flex gap-2">
              <input
                value={c}
                onChange={(e) => setC(e.target.value)}
                placeholder="Comentario íntimo…"
                className="min-w-0 flex-1 rounded-full bg-muted px-3 py-1.5 text-sm outline-none"
              />
              <button
                onClick={() => {
                  if (c.trim()) {
                    onChange({ ...p, comments: [...p.comments, { by: "Alberto", text: c }] });
                    setC("");
                  }
                }}
                className="text-primary"
              >
                <Send className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </article>
  );
}

function Voice({ secs }: { secs: number }) {
  const [playing, setPlaying] = useState(false);
  const [pos, setPos] = useState(0);
  useEffect(() => {
    if (!playing) return;
    const t = setInterval(
      () =>
        setPos((x) => {
          if (x >= secs) {
            setPlaying(false);
            return 0;
          }
          return x + 0.1;
        }),
      100,
    );
    return () => clearInterval(t);
  }, [playing, secs]);
  const bars = [4, 9, 6, 12, 8, 14, 5, 10, 7, 13, 6, 9, 11, 4, 8, 12, 6, 10, 5, 7];
  return (
    <div className="flex items-center gap-3 rounded-full bg-muted px-2 py-1.5">
      <button
        onClick={() => setPlaying(!playing)}
        className="grid h-8 w-8 shrink-0 place-items-center rounded-full bg-rose text-primary-foreground"
      >
        {playing ? <Pause className="h-3.5 w-3.5" /> : <Play className="h-3.5 w-3.5" />}
      </button>
      <div className="flex flex-1 items-center gap-[3px]">
        {bars.map((h, i) => (
          <span
            key={i}
            className={`w-[3px] rounded-full ${i / bars.length <= pos / secs ? "bg-primary" : "bg-muted-foreground/40"}`}
            style={{ height: h * 1.6 }}
          />
        ))}
      </div>
      <span className="pr-2 text-[11px] tabular-nums text-muted-foreground">
        0:{String(Math.round(secs - pos)).padStart(2, "0")}
      </span>
    </div>
  );
}

function StoryViewer({ s, onClose }: { s: { name: string; img: string }; onClose: () => void }) {
  const [p, setP] = useState(0);
  useEffect(() => {
    const t = setInterval(() => setP((x) => (x >= 100 ? 100 : x + 2)), 100);
    return () => clearInterval(t);
  }, []);
  useEffect(() => {
    if (p >= 100) onClose();
  }, [p, onClose]);
  return (
    <div className="fixed inset-0 z-50 mx-auto flex max-w-md flex-col bg-background">
      <div className="m-3 h-1 overflow-hidden rounded-full bg-muted">
        <div className="h-full bg-rose" style={{ width: `${p}%` }} />
      </div>
      <div className="flex items-center justify-between px-4">
        <p className="text-sm font-semibold">{s.name} · hace 4 h</p>
        <button onClick={onClose}>
          <X className="h-5 w-5" />
        </button>
      </div>
      <img src={s.img} alt="" className="m-3 flex-1 rounded-3xl object-cover" />
    </div>
  );
}
