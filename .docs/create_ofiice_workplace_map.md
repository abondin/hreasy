# Create new map for office workplace

## Common instruction

1. Install and run Draw.IO
2. Enable `plugins/svgdata.js` plugin. (probable you have to run `draw.io.exe --enable-plugins`)
3. Load `hreasy_workplace.xml` library
4. Draw your office location map and add workplaces from library
5. For every workplace open edit dialog (double click) and set `workplaceName` property
6. Export as svg
   ![img.png](create_office_workplace_map_img1.png)

7. In HR Easy open dictionary admin page and select office workplaces
8. Upload svg map
9. In Employee admin page populate office location property and workplace property
   ![img.png](create_office_workplace_map_img2.png)

## Data Mapping

1. SVG exported diagram's name must be set to `map` attribute of office location. It is possible to have one map for
   several office locations
2. `workplaceName` in draw.io diagram must be equal to employee `workplace name` in HR Easy  