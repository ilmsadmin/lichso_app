import { Metadata } from "next";
import { APP_NAME } from "@/lib/constants";
import { Code2, Globe, Heart, Lightbulb, Rocket, Shield, Target, Users } from "lucide-react";

export const metadata: Metadata = {
  title: `About - ${APP_NAME}`,
  description: `Learn more about ${APP_NAME} — a production-ready admin platform.`,
};

const values = [
  {
    icon: Lightbulb,
    title: "Innovation",
    description:
      "We continuously explore new technologies and approaches to deliver the best developer experience.",
  },
  {
    icon: Shield,
    title: "Security",
    description:
      "Security is not an afterthought. Every feature is built with security best practices from the ground up.",
  },
  {
    icon: Heart,
    title: "Open Source",
    description:
      "We believe in the power of open source. Our platform is transparent, community-driven, and free to use.",
  },
  {
    icon: Rocket,
    title: "Performance",
    description:
      "Speed matters. We optimize every layer — from database queries to frontend rendering — for maximum performance.",
  },
];

const timeline = [
  {
    year: "2024 Q1",
    title: "Project Inception",
    description:
      "Started as an internal tool to streamline admin panel development across projects.",
  },
  {
    year: "2024 Q2",
    title: "Core Architecture",
    description:
      "Established the Clean Architecture pattern with Go Fiber backend and Next.js frontend.",
  },
  {
    year: "2024 Q3",
    title: "Auth & RBAC",
    description:
      "Implemented JWT authentication, refresh tokens, and a flexible role-based access control system.",
  },
  {
    year: "2024 Q4",
    title: "Public Release",
    description:
      "Launched as an open-source project with comprehensive documentation and Docker deployment.",
  },
];

export default function AboutPage() {
  return (
    <div className="flex flex-col">
      {/* Hero */}
      <section className="from-background to-muted/30 border-b bg-gradient-to-b py-20 lg:py-28">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-3xl text-center">
            <h1 className="mb-6 text-4xl font-bold tracking-tight sm:text-5xl">About {APP_NAME}</h1>
            <p className="text-muted-foreground text-lg leading-relaxed sm:text-xl">
              We&apos;re building the foundation that lets developers focus on what matters — their
              unique features — instead of rebuilding authentication, user management, and admin
              panels from scratch.
            </p>
          </div>
        </div>
      </section>

      {/* Mission */}
      <section className="py-20 lg:py-28">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mx-auto grid max-w-5xl items-center gap-12 md:grid-cols-2">
            <div>
              <div className="bg-primary/10 text-primary mb-4 inline-flex items-center gap-2 rounded-full border px-3 py-1 text-sm font-medium">
                <Target className="h-4 w-4" />
                Our Mission
              </div>
              <h2 className="mb-4 text-3xl font-bold tracking-tight">
                Empowering Developers to Ship Faster
              </h2>
              <p className="text-muted-foreground mb-4">
                Every new project starts with the same boilerplate: setting up auth, building user
                management, configuring RBAC, and creating an admin dashboard. That&apos;s weeks of
                work before writing a single line of business logic.
              </p>
              <p className="text-muted-foreground">
                {APP_NAME} eliminates that overhead. We provide a production-ready, fully
                customizable admin platform that you can deploy in minutes and extend to fit your
                exact needs.
              </p>
            </div>
            <div className="grid grid-cols-2 gap-4">
              {[
                { icon: Code2, label: "Clean Code", value: "Architecture" },
                { icon: Globe, label: "Full Stack", value: "Go + Next.js" },
                { icon: Users, label: "Community", value: "Open Source" },
                { icon: Shield, label: "Enterprise", value: "Security" },
              ].map((item) => (
                <div key={item.label} className="bg-card rounded-xl border p-5 text-center">
                  <item.icon className="text-primary mx-auto mb-2 h-8 w-8" />
                  <div className="text-sm font-semibold">{item.label}</div>
                  <div className="text-muted-foreground text-xs">{item.value}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Values */}
      <section className="bg-muted/30 border-y py-20 lg:py-28">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mx-auto mb-16 max-w-2xl text-center">
            <h2 className="mb-4 text-3xl font-bold tracking-tight">Our Values</h2>
            <p className="text-muted-foreground text-lg">
              The principles that guide every decision we make.
            </p>
          </div>
          <div className="mx-auto grid max-w-4xl gap-8 sm:grid-cols-2">
            {values.map((value) => (
              <div key={value.title} className="bg-card flex gap-4 rounded-xl border p-6">
                <div className="bg-primary/10 text-primary flex h-12 w-12 shrink-0 items-center justify-center rounded-lg">
                  <value.icon className="h-6 w-6" />
                </div>
                <div>
                  <h3 className="mb-1 font-semibold">{value.title}</h3>
                  <p className="text-muted-foreground text-sm">{value.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Timeline */}
      <section className="py-20 lg:py-28">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mx-auto mb-16 max-w-2xl text-center">
            <h2 className="mb-4 text-3xl font-bold tracking-tight">Our Journey</h2>
            <p className="text-muted-foreground text-lg">
              Key milestones in the development of {APP_NAME}.
            </p>
          </div>
          <div className="mx-auto max-w-2xl">
            <div className="before:bg-border relative space-y-8 before:absolute before:top-2 before:left-4 before:h-[calc(100%-16px)] before:w-0.5 sm:before:left-1/2 sm:before:-translate-x-px">
              {timeline.map((item, index) => (
                <div
                  key={item.year}
                  className={`relative flex items-start gap-4 sm:gap-8 ${
                    index % 2 === 0 ? "sm:flex-row" : "sm:flex-row-reverse sm:text-right"
                  }`}
                >
                  <div className="hidden flex-1 sm:block">
                    {index % 2 === 0 ? (
                      <div>
                        <div className="text-primary text-sm font-medium">{item.year}</div>
                        <h3 className="mt-1 font-semibold">{item.title}</h3>
                        <p className="text-muted-foreground mt-1 text-sm">{item.description}</p>
                      </div>
                    ) : null}
                  </div>
                  <div className="border-primary bg-background relative z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full border-2 sm:mx-0">
                    <div className="bg-primary h-2.5 w-2.5 rounded-full" />
                  </div>
                  <div className="flex-1">
                    {index % 2 !== 0 || true ? (
                      <div className="sm:hidden">
                        <div className="text-primary text-sm font-medium">{item.year}</div>
                        <h3 className="mt-1 font-semibold">{item.title}</h3>
                        <p className="text-muted-foreground mt-1 text-sm">{item.description}</p>
                      </div>
                    ) : null}
                    <div className="hidden sm:block">
                      {index % 2 !== 0 ? (
                        <div>
                          <div className="text-primary text-sm font-medium">{item.year}</div>
                          <h3 className="mt-1 font-semibold">{item.title}</h3>
                          <p className="text-muted-foreground mt-1 text-sm">{item.description}</p>
                        </div>
                      ) : null}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
