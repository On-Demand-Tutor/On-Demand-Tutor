import { describe, it, expect, beforeEach, jest } from "@jest/globals";
import { JSDOM } from "jsdom";
import "../booking";

describe("BookingApp", () => {
  let dom: JSDOM;
  let document: Document;

  beforeEach(() => {
    dom = new JSDOM(`
      <form id="bookingForm">
        <select id="tutor"><option value="Nguyễn Văn A">Nguyễn Văn A</option></select>
        <input type="date" id="date" value="2025-09-12" />
        <input type="time" id="time" value="10:00" />
        <textarea id="note">Test note</textarea>
        <button type="submit">Submit</button>
      </form>
      <table id="bookingTable"><tbody></tbody></table>
    `);
    document = dom.window.document;
    (global as any).document = document;
    (global as any).window = dom.window;
  });

  it("should add a booking row when form is submitted", () => {
    const form = document.getElementById("bookingForm") as HTMLFormElement;
    const tbody = document.querySelector("#bookingTable tbody") as HTMLTableSectionElement;

    // Fake submit event
    form.dispatchEvent(new dom.window.Event("submit", { bubbles: true, cancelable: true }));

    expect(tbody.children.length).toBe(1);
    expect(tbody.children[0].querySelector("td")?.textContent).toContain("Nguyễn Văn A");
  });
});