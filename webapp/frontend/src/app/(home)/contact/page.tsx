"use client";

import { useState } from "react";
import { Metadata } from "next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { Mail, MapPin, Phone, Send, MessageSquare, Clock } from "lucide-react";
import { APP_NAME } from "@/lib/constants";

const contactInfo = [
  {
    icon: Mail,
    title: "Email",
    description: "Our support team will get back to you within 24 hours.",
    value: "support@zplus.dev",
    href: "mailto:support@zplus.dev",
  },
  {
    icon: MapPin,
    title: "Office",
    description: "Visit us at our headquarters.",
    value: "Ho Chi Minh City, Vietnam",
    href: "#",
  },
  {
    icon: Phone,
    title: "Phone",
    description: "Mon-Fri from 8am to 5pm (GMT+7).",
    value: "+84 (0) 123 456 789",
    href: "tel:+84123456789",
  },
  {
    icon: Clock,
    title: "Working Hours",
    description: "We're available during business hours.",
    value: "Mon - Fri, 8:00 - 17:00",
    href: "#",
  },
];

export default function ContactPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    subject: "",
    message: "",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    // Simulate form submission
    await new Promise((resolve) => setTimeout(resolve, 1500));

    toast.success("Message sent successfully!", {
      description: "We'll get back to you within 24 hours.",
    });

    setFormData({ name: "", email: "", subject: "", message: "" });
    setIsSubmitting(false);
  };

  return (
    <div className="flex flex-col">
      {/* Hero */}
      <section className="from-background to-muted/30 border-b bg-gradient-to-b py-20 lg:py-28">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-3xl text-center">
            <div className="bg-background/80 mb-6 inline-flex items-center gap-2 rounded-full border px-4 py-1.5 text-sm font-medium">
              <MessageSquare className="text-primary h-4 w-4" />
              <span>Get In Touch</span>
            </div>
            <h1 className="mb-6 text-4xl font-bold tracking-tight sm:text-5xl">Contact Us</h1>
            <p className="text-muted-foreground text-lg sm:text-xl">
              Have a question or need help? We&apos;d love to hear from you. Fill out the form below
              or reach us through any of our channels.
            </p>
          </div>
        </div>
      </section>

      {/* Contact Info Cards */}
      <section className="bg-muted/30 border-b py-12">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mx-auto grid max-w-5xl gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {contactInfo.map((info) => (
              <a
                key={info.title}
                href={info.href}
                className="group bg-card hover:border-primary/50 rounded-xl border p-5 transition-all hover:shadow-md"
              >
                <div className="bg-primary/10 text-primary group-hover:bg-primary group-hover:text-primary-foreground mb-3 flex h-10 w-10 items-center justify-center rounded-lg transition-colors">
                  <info.icon className="h-5 w-5" />
                </div>
                <h3 className="mb-1 font-semibold">{info.title}</h3>
                <p className="text-muted-foreground mb-2 text-xs">{info.description}</p>
                <p className="text-primary text-sm font-medium">{info.value}</p>
              </a>
            ))}
          </div>
        </div>
      </section>

      {/* Contact Form */}
      <section className="py-20 lg:py-28">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-2xl">
            <div className="mb-10 text-center">
              <h2 className="mb-4 text-3xl font-bold tracking-tight">Send Us a Message</h2>
              <p className="text-muted-foreground">
                Fill out the form and our team will respond within 24 hours.
              </p>
            </div>

            <form
              onSubmit={handleSubmit}
              className="bg-card space-y-6 rounded-xl border p-6 sm:p-8"
            >
              <div className="grid gap-6 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="name">Full Name</Label>
                  <Input
                    id="name"
                    name="name"
                    placeholder="John Doe"
                    value={formData.name}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email Address</Label>
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    placeholder="john@example.com"
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="subject">Subject</Label>
                <Select
                  value={formData.subject}
                  onValueChange={(value) => setFormData((prev) => ({ ...prev, subject: value }))}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a subject" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="general">General Inquiry</SelectItem>
                    <SelectItem value="support">Technical Support</SelectItem>
                    <SelectItem value="bug">Bug Report</SelectItem>
                    <SelectItem value="feature">Feature Request</SelectItem>
                    <SelectItem value="partnership">Partnership</SelectItem>
                    <SelectItem value="other">Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="message">Message</Label>
                <Textarea
                  id="message"
                  name="message"
                  placeholder="Tell us how we can help..."
                  rows={6}
                  value={formData.message}
                  onChange={handleChange}
                  required
                />
              </div>

              <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
                {isSubmitting ? (
                  <>Sending...</>
                ) : (
                  <>
                    <Send className="mr-2 h-4 w-4" />
                    Send Message
                  </>
                )}
              </Button>
            </form>
          </div>
        </div>
      </section>
    </div>
  );
}
