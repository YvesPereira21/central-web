import { Component, input, computed } from '@angular/core';

@Component({
  selector: 'app-level-stage',
  standalone: true,
  templateUrl: './level-stage.component.html'
})
export class LevelStageComponent {
  level = input.required<string>();

  badgeColor = computed(() => {
    const currentLevel = this.level();
    if (!currentLevel) return 'text-gray-500 bg-gray-500/15 border-gray-500/30';

    switch (currentLevel.toLowerCase()) {
      case 'novato':
        return 'text-emerald-500 bg-emerald-500/15 border-emerald-500/30';
      case 'praticante':
        return 'text-lime-500 bg-lime-500/15 border-lime-500/30';
      case 'veterano':
        return 'text-yellow-500 bg-yellow-500/15 border-yellow-500/30';
      case 'visionário':
        return 'text-amber-500 bg-amber-500/15 border-amber-500/30';
      case 'domador de legado':
        return 'text-orange-500 bg-orange-500/15 border-orange-500/30';
      case 'compilador humano':
        return 'text-rose-500 bg-rose-500/15 border-rose-500/30';
      default:
        return 'text-gray-500 bg-gray-500/15 border-gray-500/30';
    }
  });
}
