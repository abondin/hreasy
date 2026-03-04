import type { Employee } from "@/services/employee.service";

export type WorkplaceClickHandler = (
  workplaceName: string,
  employee?: Employee,
) => void;

export function initializeWorkplaces(
  svg: SVGElement,
  workplaceClickHandler?: WorkplaceClickHandler,
  employees?: Employee[],
  highlightedWorkplaces?: string[],
) {
  const workplaces = svg.querySelectorAll<SVGElement>(
    "[data-workplaceName][data-workplaceType='1']",
  );

  workplaces.forEach((workplace) => {
    const workplaceName = getWorkplaceName(workplace);
    attachHoverEffect(workplace);

    const employeeNode = workplace.parentElement?.querySelector("[data-empl]");
    if (employeeNode && workplaceName) {
      const employee = findEmployee(workplaceName, employees);
      updateEmployeeText(workplace as SVGGraphicsElement, employeeNode, employee);

      if (
        highlightedWorkplaces &&
        highlightedWorkplaces.some(
          (name) => normalizeWorkplaceName(name) === workplaceName,
        )
      ) {
        highlightWorkplace(workplace);
      }

      if (workplaceClickHandler) {
        workplace.style.cursor = "pointer";
        workplace.addEventListener("click", () => {
          workplaceClickHandler(workplaceName, employee);
        });
      }

      addTooltip(workplace, employee, workplaceName);
    }
  });
}

function getWorkplaceName(workplace: SVGElement): string | undefined {
  return normalizeWorkplaceName(workplace.dataset.workplacename);
}

function normalizeWorkplaceName(value?: string | null): string | undefined {
  const trimmed = value?.trim().toLowerCase();
  return trimmed && trimmed.length > 0 ? trimmed : undefined;
}

function attachHoverEffect(workplace: SVGElement) {
  workplace.addEventListener("mouseenter", () => {
    workplace.style.strokeWidth = "3px";
  });
  workplace.addEventListener("mouseleave", () => {
    workplace.style.strokeWidth = "1px";
  });
}

function findEmployee(
  workplaceName: string | undefined,
  employees?: Employee[],
): Employee | undefined {
  return employees?.find((employee) => {
    const employeeWorkplace = normalizeWorkplaceName(employee.officeWorkplace);
    return employeeWorkplace && employeeWorkplace === workplaceName;
  });
}

function updateEmployeeText(
  workplaceElement: SVGGraphicsElement,
  employeeNode: Element,
  employee?: Employee,
) {
  const maxLength = Math.round(workplaceElement.getBBox().width * 0.95);
  const ellipsis = "...";

  employeeNode.querySelectorAll("text").forEach((textElement) => {
    if (textElement.textContent?.includes("${empl}")) {
      let employeeName = employee?.displayName ?? "";
      textElement.textContent = employeeName;
      textElement.setAttribute("role", "button");
      textElement.style.cursor = "pointer";

      let textWidth = textElement.getBBox().width;
      while (textWidth > maxLength && employeeName.length > 0) {
        employeeName = employeeName.slice(0, -1);
        textElement.textContent = `${employeeName}${ellipsis}`;
        textWidth = textElement.getBBox().width;
      }
    }
  });
}

function highlightWorkplace(workplace: SVGElement) {
  workplace.querySelectorAll("rect").forEach((rect) => {
    rect.style.filter = "drop-shadow(2px 2px 4px rgba(255, 50, 50, 0.7))";
  });
}

function addTooltip(
  workplace: SVGElement,
  employee?: Employee,
  workplaceName?: string,
) {
  let titleElement = workplace.querySelector("title") as
    | SVGTitleElement
    | null;
  if (!titleElement) {
    titleElement = document.createElementNS(
      "http://www.w3.org/2000/svg",
      "title",
    ) as SVGTitleElement;
    workplace.appendChild(titleElement);
  }

  const lines: string[] = [];
  if (workplaceName) {
    lines.push(workplaceName);
  }
  if (employee?.displayName) {
    lines.push(employee.displayName);
    if (employee.currentProject?.name) {
      const projectLine = employee.currentProject.role
        ? `${employee.currentProject.name} - ${employee.currentProject.role}`
        : employee.currentProject.name;
      lines.push(projectLine);
    }
  }

  titleElement.textContent = lines.join("\n");
}
