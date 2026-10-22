import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'experienceLevel',
  standalone: true
})
export class ExperienceLevelPipe implements PipeTransform {
  private readonly levels: Record<string, string> = {
    'JR': 'Júnior',
    'JUNIOR': 'Júnior',
    'PL': 'Pleno',
    'MID': 'Pleno',
    'MID_LEVEL': 'Pleno',
    'SR': 'Sênior',
    'SENIOR': 'Sênior'
  };

  transform(value: string | null | undefined): string {
    if (!value) return '';
    return this.levels[value.toUpperCase()] || value;
  }
}
