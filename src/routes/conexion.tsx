import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Lock, Send, ChefHat, Camera, Music, Moon, Plus, EyeOff } from "lucide-react";
import { Page } from "@/components/BottomNav";

export const Route = createFileRoute("/conexion")({
  head: () => ({
    meta: [
      { title: "Conexión — Preguntas diarias y retos" },
      { name: "description", content: "Preguntas en doble ciega, retos cooperativos y preguntas secretas." },
      { property: "og:title", content: "Conexión — Preguntas diarias y retos" },
      { property: "og:description", content: "Preguntas en doble ciega, retos cooperativos y preguntas secretas." },
    ],
  }),
  component: Conexion,
});

const tabs = ["Diaria", "Retos", "Secretas"] as const;

function Conexion() {
  const [tab, setTab] = useState<(typeof tabs)[number]>("Diaria");
  return (
    <Page title="Conexión" subtitle="Día 412 juntos">
      <div className="glass mb-5 grid grid-cols-3 rounded-full p-1">
        {tabs.map((t) => (
          <button key={t} onClick={() => setTab(t)} className={`rounded-full py-2 text-sm font-medium transition ${tab === t ? "bg-rose text-primary-foreground" : "text-muted-foreground"}`}>{t}</button>
        ))}
      </div>
      {tab === "Diaria" && <Daily />}
      {tab === "Retos" && <Challenges />}
      {tab === "Secretas" && <Secret />}
    </Page>
  );
}

function Daily() {
  const [answer, setAnswer] = useState("");
  const [sent, setSent] = useState(false);
  const [chat, setChat] = useState<{ by: string; text: string }[]>([{ by: "Yuki", text: "Jajaja sabía que dirías eso 🥹" }]);
  const [msg, setMsg] = useState("");
  return (
    <div className="space-y-4">
      <div className="glass rounded-3xl p-5">
        <p className="text-xs uppercase tracking-[0.2em] text-primary">Pregunta del día</p>
        <h2 className="mt-2 text-3xl font-semibold leading-tight">¿Qué pequeño gesto mío te hizo sentir más querida esta semana?</h2>
      </div>

      <div className="glass rounded-3xl p-4">
        <p className="mb-2 text-xs text-muted-foreground">Tu respuesta</p>
        {sent ? (
          <p className="text-sm">{answer}</p>
        ) : (
          <div className="space-y-2">
            <textarea value={answer} onChange={(e) => setAnswer(e.target.value)} rows={3} placeholder="Escribe con el corazón…" className="w-full resize-none rounded-2xl bg-muted p-3 text-sm outline-none focus:ring-1 focus:ring-ring" />
            <button disabled={!answer.trim()} onClick={() => setSent(true)} className="w-full rounded-full bg-rose py-2.5 text-sm font-semibold text-primary-foreground disabled:opacity-40">Enviar y revelar</button>
          </div>
        )}
      </div>

      <div className="glass relative overflow-hidden rounded-3xl p-4">
        <p className="mb-2 text-xs text-muted-foreground">Respuesta de Yuki</p>
        <p className={`text-sm transition-all duration-700 ${sent ? "" : "select-none blur-md"}`}>
          Cuando me mandaste el audio cantando a las 3 de la mañana solo para que me durmiera. Me hizo llorar de lo bonito.
        </p>
        {!sent && (
          <div className="absolute inset-0 grid place-items-center bg-card/30">
            <div className="flex items-center gap-2 rounded-full bg-muted px-4 py-2 text-xs"><Lock className="h-3.5 w-3.5 text-primary" />Responde para desbloquear</div>
          </div>
        )}
      </div>

      {sent && (
        <div className="glass rounded-3xl p-4 animate-in fade-in slide-in-from-bottom-2">
          <p className="mb-3 text-xs text-muted-foreground">Comentad vuestras respuestas</p>
          <div className="mb-3 space-y-2">
            {chat.map((c, i) => (
              <div key={i} className={`max-w-[80%] rounded-2xl px-3 py-2 text-sm ${c.by === "Tú" ? "ml-auto bg-rose text-primary-foreground" : "bg-muted"}`}>{c.text}</div>
            ))}
          </div>
          <div className="flex gap-2">
            <input value={msg} onChange={(e) => setMsg(e.target.value)} onKeyDown={(e) => { if (e.key === "Enter" && msg.trim()) { setChat([...chat, { by: "Tú", text: msg }]); setMsg(""); } }} placeholder="Mensaje…" className="min-w-0 flex-1 rounded-full bg-muted px-4 py-2 text-sm outline-none" />
            <button onClick={() => { if (msg.trim()) { setChat([...chat, { by: "Tú", text: msg }]); setMsg(""); } }} className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-rose text-primary-foreground"><Send className="h-4 w-4" /></button>
          </div>
        </div>
      )}
    </div>
  );
}

function Challenges() {
  const [items, setItems] = useState([
    { icon: ChefHat, title: "Cocinar lo mismo", desc: "Preparad la misma receta y cenad en videollamada.", me: true, them: false },
    { icon: Camera, title: "Foto espontánea", desc: "Mandad una foto de lo que veis ahora mismo, sin filtros.", me: true, them: true },
    { icon: Music, title: "Playlist cruzada", desc: "Añadid 5 canciones que os recuerden al otro.", me: false, them: true },
    { icon: Moon, title: "Misma luna", desc: "Fotografiad la luna desde vuestra ciudad esta noche.", me: false, them: false },
  ]);
  return (
    <div className="space-y-3">
      {items.map((c, i) => {
        const Icon = c.icon;
        const done = c.me && c.them;
        return (
          <div key={c.title} className={`glass rounded-3xl p-4 ${done ? "shadow-glow" : ""}`}>
            <div className="flex gap-3">
              <div className="grid h-11 w-11 shrink-0 place-items-center rounded-2xl bg-muted"><Icon className="h-5 w-5 text-primary" /></div>
              <div className="min-w-0 flex-1">
                <p className="font-semibold">{c.title}</p>
                <p className="text-xs text-muted-foreground">{c.desc}</p>
              </div>
            </div>
            <div className="mt-3 flex items-center justify-between">
              <div className="flex gap-2 text-[11px]">
                <span className={`rounded-full px-2 py-0.5 ${c.me ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"}`}>Tú {c.me ? "✓" : "…"}</span>
                <span className={`rounded-full px-2 py-0.5 ${c.them ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"}`}>Yuki {c.them ? "✓" : "…"}</span>
              </div>
              {done ? <span className="text-xs text-primary">¡Completado! 💞</span> : (
                <button onClick={() => setItems(items.map((x, j) => j === i ? { ...x, me: !x.me } : x))} className="rounded-full border border-primary px-3 py-1 text-xs text-primary">{c.me ? "Deshacer" : "Hecho"}</button>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}

function Secret() {
  const [qs, setQs] = useState([
    { q: "¿Cuál fue el primer momento en que supiste que me querías?", from: "Yuki", answered: false },
    { q: "Si pudieras revivir un día conmigo, ¿cuál sería?", from: "Tú", answered: true },
  ]);
  const [text, setText] = useState("");
  return (
    <div className="space-y-3">
      <div className="glass rounded-3xl p-4">
        <p className="mb-2 flex items-center gap-2 text-sm font-semibold"><EyeOff className="h-4 w-4 text-primary" />Crea una pregunta secreta</p>
        <div className="flex gap-2">
          <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Solo Yuki podrá verla…" className="min-w-0 flex-1 rounded-full bg-muted px-4 py-2 text-sm outline-none" />
          <button onClick={() => { if (text.trim()) { setQs([{ q: text, from: "Tú", answered: false }, ...qs]); setText(""); } }} className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-rose text-primary-foreground"><Plus className="h-4 w-4" /></button>
        </div>
      </div>
      {qs.map((q, i) => (
        <div key={i} className="glass rounded-3xl p-4">
          <p className="text-[11px] uppercase tracking-widest text-muted-foreground">De {q.from}</p>
          <p className="mt-1 font-display text-xl">{q.q}</p>
          <p className="mt-2 text-xs text-primary">{q.answered ? "Respondida ✓" : q.from === "Tú" ? "Esperando respuesta…" : "Toca para responder"}</p>
        </div>
      ))}
    </div>
  );
}
