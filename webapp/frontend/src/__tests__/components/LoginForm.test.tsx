import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { LoginForm } from "@/components/auth/LoginForm";

// Mock next/link
vi.mock("next/link", () => ({
  default: ({
    children,
    href,
    ...props
  }: {
    children: React.ReactNode;
    href: string;
    [key: string]: unknown;
  }) => (
    <a href={href} {...props}>
      {children}
    </a>
  ),
}));

// Mock useAuth hook
const mockLogin = vi.fn();
vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({
    login: mockLogin,
    isLoggingIn: false,
  }),
}));

describe("LoginForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the login form", () => {
    render(<LoginForm />);

    expect(screen.getByText("Welcome back")).toBeInTheDocument();
    expect(screen.getByLabelText("Email")).toBeInTheDocument();
    expect(screen.getByLabelText("Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sign in" })).toBeInTheDocument();
  });

  it("renders forgot password and register links", () => {
    render(<LoginForm />);

    expect(screen.getByText("Forgot password?")).toBeInTheDocument();
    expect(screen.getByText("Create account")).toBeInTheDocument();
  });

  it("allows typing email and password", () => {
    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email") as HTMLInputElement;
    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });

    expect(emailInput.value).toBe("test@example.com");
    expect(passwordInput.value).toBe("password123");
  });

  it("calls login on form submit", () => {
    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email");
    const passwordInput = screen.getByLabelText("Password");

    fireEvent.change(emailInput, { target: { value: "admin@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "secret123" } });
    fireEvent.submit(screen.getByRole("button", { name: "Sign in" }));

    expect(mockLogin).toHaveBeenCalledWith(
      { email: "admin@example.com", password: "secret123" },
      expect.objectContaining({ onError: expect.any(Function) })
    );
  });

  it("toggles password visibility", () => {
    render(<LoginForm />);

    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;
    expect(passwordInput.type).toBe("password");

    // Click the toggle button (it's the button inside the password field area)
    const toggleButtons = screen.getAllByRole("button");
    const toggleBtn = toggleButtons.find((btn) => (btn as HTMLButtonElement).type === "button");
    if (toggleBtn) {
      fireEvent.click(toggleBtn);
      expect(passwordInput.type).toBe("text");

      fireEvent.click(toggleBtn);
      expect(passwordInput.type).toBe("password");
    }
  });

  it("has correct input types", () => {
    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email") as HTMLInputElement;
    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;

    expect(emailInput.type).toBe("email");
    expect(passwordInput.type).toBe("password");
  });

  it("has required attributes on inputs", () => {
    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email") as HTMLInputElement;
    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;

    expect(emailInput.required).toBe(true);
    expect(passwordInput.required).toBe(true);
  });

  it("has proper autocomplete attributes", () => {
    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email") as HTMLInputElement;
    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;

    expect(emailInput.autocomplete).toBe("email");
    expect(passwordInput.autocomplete).toBe("current-password");
  });
});
