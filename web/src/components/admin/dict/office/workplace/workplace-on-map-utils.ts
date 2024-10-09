import {EmployeeOnWorkplace} from "@/components/admin/dict/office/workplace/MapPreviewDataContainer";

export default class WorkplaceOnMapUtils {

    public static defaultMapSizes = {
        width: 800,
        height: 600,
        employeeNameTextAreaWidth: 200,
    };

    public static adjustSvgViewBox(svg: SVGElement) {
        const svgElement = svg.querySelector('svg');
        if (svgElement && !svgElement.hasAttribute('viewBox')) {
            // Original svg size
            const svgWidth = svgElement.getAttribute('width') || this.defaultMapSizes.width;
            const svgHeight = svgElement.getAttribute('height') || this.defaultMapSizes.height;

            // Adjust to default map size
            svgElement.setAttribute('viewBox', `0 0 ${svgWidth} ${svgHeight}`);
            svgElement.setAttribute('width', `${this.defaultMapSizes.width}`);
            svgElement.setAttribute('height', `${this.defaultMapSizes.height}`);
            svgElement.setAttribute('preserveAspectRatio', 'xMidYMin meet');
        }
    }

    public static initializeWorkplace(svg: SVGElement, employees?: EmployeeOnWorkplace[]) {
        const workplaces = svg.querySelectorAll('[data-workplaceName][data-workplaceType="1"]');

        workplaces.forEach((element) => {
            const workplace = element as SVGElement;
            const workplaceName = this.getWorkplaceName(workplace);
            this.attachHoverEffect(workplace);

            const emplElement = workplace.parentElement?.querySelector('[data-empl]');
            if (emplElement) {
                const employee = this.findEmployee(workplaceName, employees);
                this.updateEmployeeText(workplace as SVGGraphicsElement, emplElement, employee);
                this.highlightSelectedEmployee(workplace, employee);
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
    private static findEmployee(workplaceName: string | undefined, employees?: EmployeeOnWorkplace[]): EmployeeOnWorkplace | undefined {
        return employees?.find(x => x.workplaceName && x.workplaceName.trim().toLowerCase() === workplaceName);
    }

// Update the text element with employee's name and adjust the length
    private static updateEmployeeText(workplaceElement: SVGGraphicsElement, emplElement: Element, employee?: EmployeeOnWorkplace): void {
        const maxLength = Math.round(workplaceElement.getBBox().width*0.95);
        const ellipsis = '...';

        emplElement.querySelectorAll('text').forEach((textElement) => {
            if (textElement.textContent?.includes('${empl}')) {
                let employeeName = employee?.employeeDisplayName || '';
                textElement.textContent = employeeName;

                let textWidth = textElement.getBBox().width;

                while (textWidth > maxLength && employeeName.length > 0) {
                    employeeName = employeeName.slice(0, -1);
                    textElement.textContent = employeeName + ellipsis;
                    textWidth = textElement.getBBox().width;
                }
            }
        });
    }

// Highlight the workplace if the employee is selected
    private static highlightSelectedEmployee(workplace: SVGElement, employee?: EmployeeOnWorkplace): void {
        if (employee?.selected) {
            workplace.querySelectorAll('rect').forEach(rect=>{
                rect.style.filter = 'drop-shadow(2px 2px 4px rgba(255, 50, 50, 0.7))';
            })
        }
    }

    private static addTooltip(workplace: SVGElement, employee?: EmployeeOnWorkplace, workplaceName?: string): void {
        let titleElement: Element|null = workplace.querySelector('title');
        if (!titleElement) {
            titleElement = document.createElementNS('http://www.w3.org/2000/svg', 'title');
            workplace.appendChild(titleElement);
        }
        let title = '';
        if (workplaceName){
            title+=`${workplaceName}`;
        }
        if (employee?.employeeDisplayName){
            title+=`\n${employee?.employeeDisplayName}`;
        }
        titleElement.textContent = title;
    }



}
