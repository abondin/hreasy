Component: src/components/vacations/VacationsTimeline.vue

Purpose
- Timeline visualization of vacations using vis-timeline.

Props
- vacations: Vacation[] (required)
- year: number (required)

State
- timeline: vis Timeline instance
- items: DataSet
- groups: DataSet

Lifecycle/Watchers
- mounted(): initTimeline()
- watch vacations (immediate, deep) and year -> updateTimelineData()
- beforeDestroy(): destroy timeline

Timeline options
- stack: false
- groupOrder: alphabetic by group content
- start/end: first/last day of selected year
- editable: false, zoomable: true, zoomKey: ctrlKey
- orientation: both, multiselect: false, selectable: false
- tooltip followMouse, locale: 'ru'

Data mapping
- groups: unique employees from vacations, content = employeeDisplayName (fallback 'unknown_employee')
- items: each vacation as a bar; end date = endDate + 1 day; title includes employeeDisplayName, date range, and status label.
- status colors via getStatusColor:
  - PLANNED: cyan.darken3
  - TAKEN: green.darken1
  - COMPENSATION: green.darken4
  - CANCELED: theme error or red.darken2
  - REJECTED: grey.darken2
  - REQUESTED: deepOrange.lighten4

Events
- Emits "year-navigation" when timeline window midpoint crosses into another year.

Notes for migration
- Preserve year navigation behavior and status color mapping.
- Maintain tooltip content format.
- Keep +1 day endDate extension for inclusive range rendering.
