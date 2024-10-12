import {Employee} from "@/components/empl/employee.service";
import logger from "@/logger";

export default class WorkplaceOnMapUtils {

    public static adjustSvgViewBox(svg: SVGElement, width: number, height: number) {
        const svgElement = svg.querySelector('svg');
        if (svgElement) {
            // Original svg size
            const svgWidth = svgElement.width.baseVal.value;
            const svgHeight = svgElement.height.baseVal.value;

            // Scale
            const scaleX = width / svgWidth;
            const scaleY = height / svgHeight;

            // Minimum scale
            const scale = Math.min(scaleX, scaleY);

            if (scale < 1) {
                svgElement.setAttribute(
                    "viewBox",
                    `0 0 ${svgWidth} ${svgHeight}`
                );
                svgElement.setAttribute("width", `${svgWidth * scale}px`);
                svgElement.setAttribute("height", `${svgHeight * scale}px`);
                svgElement.style.height = `${svgHeight * scale}px`;
            }
        }
    }

    public static initializeWorkplace(svg: SVGElement, workplaceClickListener?: (workplaceName: string, employee?: Employee) => any, employees?: Employee[], highlightedWorkplaces?: string[]) {
        const workplaces = svg.querySelectorAll('[data-workplaceName][data-workplaceType="1"]');

        workplaces.forEach((element) => {
            const workplace = element as SVGElement;
            const workplaceName = this.getWorkplaceName(workplace);
            this.attachHoverEffect(workplace);

            const emplElement = workplace.parentElement?.querySelector('[data-empl]');
            if (emplElement && workplaceName) {
                const employee = this.findEmployee(workplaceName, employees);
                this.updateEmployeeText(workplace as SVGGraphicsElement, emplElement, employee);
                if (highlightedWorkplaces && highlightedWorkplaces.indexOf(workplaceName) >= 0) {
                    this.highlightWorkplace(workplace);
                }
                if (workplaceClickListener) {
                    workplace.style.cursor = 'pointer';
                    workplace.addEventListener('click', () => {
                        workplaceClickListener(workplaceName, employee);
                    });
                }

                this.addTooltip(workplace, employee, workplaceName);
            }
        });
    }

// Get the workplace name from the dataset
    private static getWorkplaceName(workplace: SVGElement): string | undefined {
        return workplace.dataset['workplacename']?.trim().toLowerCase();
    }

// Attach hover effect to visually highlight the workplace
    private static attachHoverEffect(workplace: SVGElement): void {
        workplace.addEventListener('mouseenter', () => {
            workplace.style.strokeWidth = '3px';
        });
        workplace.addEventListener('mouseleave', () => {
            workplace.style.strokeWidth = '1px';
        });
    }

// Find the employee corresponding to the workplace
    private static findEmployee(workplaceName: string | undefined, employees?: Employee[]): Employee | undefined {
        return employees?.find(x => x.officeWorkplace && x.officeWorkplace.trim().toLowerCase() === workplaceName);
    }

// Update the text element with employee's name and adjust the length
    private static updateEmployeeText(workplaceElement: SVGGraphicsElement, emplElement: Element, employee?: Employee): void {
        const maxLength = Math.round(workplaceElement.getBBox().width * 0.95);
        const ellipsis = '...';

        emplElement.querySelectorAll('text').forEach((textElement) => {
            if (textElement.textContent?.includes('${empl}')) {
                let employeeName = employee?.displayName || '';
                textElement.textContent = employeeName;
                textElement.style.cursor = 'pointer';

                let textWidth = textElement.getBBox().width;

                while (textWidth > maxLength && employeeName.length > 0) {
                    employeeName = employeeName.slice(0, -1);
                    textElement.textContent = employeeName + ellipsis;
                    textWidth = textElement.getBBox().width;
                }
            }
        });
    }

// Highlight the workplace
    private static highlightWorkplace(workplace: SVGElement): void {
        workplace.querySelectorAll('rect').forEach(rect => {
            rect.style.filter = 'drop-shadow(2px 2px 4px rgba(255, 50, 50, 0.7))';
        })
    }

    private static addTooltip(workplace: SVGElement, employee?: Employee, workplaceName?: string): void {
        let titleElement: Element | null = workplace.querySelector('title');
        if (!titleElement) {
            titleElement = document.createElementNS('http://www.w3.org/2000/svg', 'title');
            workplace.appendChild(titleElement);
        }
        let title = '';
        if (workplaceName) {
            title += `${workplaceName}`;
        }
        if (employee?.displayName) {
            title += `\n${employee?.displayName}`;
            if (employee.currentProject) {
                title += `\n${employee.currentProject.name}`;
                if (employee.currentProject.role) {
                    title += `- ${employee.currentProject.role}`;
                }
            }
        }
        titleElement.textContent = title;
    }


}
