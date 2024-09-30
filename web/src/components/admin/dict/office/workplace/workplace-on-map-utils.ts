import {DictOfficeWorkplace} from "@/components/admin/dict/dict.admin.service";

export default class WorkplaceOnMapUtils {
    public static getOrCreateWorkplaceIcon(svg: SVGElement, workplace: DictOfficeWorkplace, clickListener?: (workplace: DictOfficeWorkplace)=>any) {
        const icon = svg.querySelector(`[data-id='${workplace.id?.toString()}'`);

        if (workplace.mapX && workplace.mapY && !icon) {

            const newGroup = this.createGroupElement(workplace, clickListener);
            const newIcon = this.createIconElement(workplace);
            const newText = this.createTextElement(workplace);
            const tooltip = this.createTooltipElement(workplace);

            newGroup.appendChild(tooltip);
            newGroup.appendChild(newIcon);
            newGroup.appendChild(newText);

            svg.appendChild(newGroup);
        }
    }

    public static createGroupElement(workplace: DictOfficeWorkplace, clickListener?: (workplace: DictOfficeWorkplace)=>any): SVGGElement {
        const newGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        newGroup.setAttribute('data-id', workplace.id.toString());
        newGroup.setAttribute('class', 'workplace-icon');
        newGroup.style.cursor = 'pointer';

        if (clickListener) {
            newGroup.addEventListener('click', (e) => {
                clickListener(workplace);
                e.stopPropagation();
            });
        }

        return newGroup;
    }

    public static createIconElement(workplace: DictOfficeWorkplace): SVGCircleElement {
        const newIcon = document.createElementNS('http://www.w3.org/2000/svg', 'circle');

        newIcon.setAttribute('cx', workplace.mapX!.toString());
        newIcon.setAttribute('cy', workplace.mapY!.toString());
        newIcon.setAttribute('r', '25');
        newIcon.setAttribute('fill', 'lightgray');
        newIcon.setAttribute('stroke', 'black');
        newIcon.setAttribute('stroke-width', '2');

        return newIcon;
    }

    public static createTextElement(workplace: DictOfficeWorkplace): SVGTextElement {
        const newText = document.createElementNS('http://www.w3.org/2000/svg', 'text');

        newText.setAttribute('x', workplace.mapX!.toString());
        newText.setAttribute('y', workplace.mapY!.toString());
        newText.setAttribute('dy', '0.4em');
        newText.setAttribute('text-anchor', 'middle');
        newText.setAttribute('font-size', '18');
        newText.setAttribute('fill', 'white');
        newText.textContent = '🪑';

        newText.style.userSelect = 'none';

        return newText;
    }

    public static createTooltipElement(workplace: DictOfficeWorkplace): SVGTitleElement {
        const tooltip = document.createElementNS('http://www.w3.org/2000/svg', 'title');
        tooltip.textContent = `${workplace.name}. ${workplace.description || ''}`;

        return tooltip;
    }

    public static highlightWorkplace(svg: SVGElement, selectedWorkplace: DictOfficeWorkplace|null) {
        // Reset previous highlights (remove highlights from all workplaces)
        const allIcons = svg.querySelectorAll('.workplace-icon circle');
        allIcons.forEach(icon => {
            icon.setAttribute('fill', 'lightgray');
        });

        // Highlight the selected workplace
        if (selectedWorkplace) {
            const selectedIcon = svg.querySelector(`[data-id='${selectedWorkplace.id?.toString()}'] circle`);
            if (selectedIcon) {
                selectedIcon.setAttribute('fill', 'yellow');
            }
        }
    }

    public static removeAllWorkplaces(svg: SVGElement) {
        const icons = svg.querySelectorAll('.workplace-icon');
        icons.forEach(icon => {
            icon.remove();
        });
    }

}
