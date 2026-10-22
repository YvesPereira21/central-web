import { Component, inject, OnInit, signal } from '@angular/core';
import { QualificationService } from '../../../qualifications/services/qualification.service';
import { Qualification } from '../../../../core/models/qualification';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { RouterLink } from '@angular/router';
import { ExperienceLevelPipe } from '../../../../shared/pipes/experience-level.pipe';

@Component({
  selector: 'app-qualification-management',
  standalone: true,
  imports: [PaginationComponent, RouterLink, ExperienceLevelPipe],
  templateUrl: './qualification-management.component.html',
  styleUrl: './qualification-management.component.css'
})
export class QualificationManagementComponent implements OnInit {
  private qualificationService = inject(QualificationService);

  activeTab = signal<'pending' | 'verified'>('pending');
  qualifications = signal<Qualification[]>([]);
  isLoading = signal<boolean>(false);

  // Paginação
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);
  totalPages = signal<number>(0);
  totalElements = signal<number>(0);

  ngOnInit(): void {
    this.loadQualifications(0);
  }

  setTab(tab: 'pending' | 'verified'): void {
    this.activeTab.set(tab);
    this.loadQualifications(0);
  }

  loadQualifications(page: number = 0): void {
    this.isLoading.set(true);
    const size = this.pageSize();

    const request$ = this.activeTab() === 'pending'
      ? this.qualificationService.getNotVerifiedQualificationsPaged(page, size)
      : this.qualificationService.getVerifiedQualificationsPaged(page, size);

    request$.subscribe({
      next: (response) => {
        this.qualifications.set(response.content || []);
        this.currentPage.set(response.number ?? 0);
        this.totalPages.set(response.totalPages ?? 0);
        this.totalElements.set(response.totalElements ?? 0);
        this.isLoading.set(false);
      },
      error: () => {
        this.qualifications.set([]);
        this.isLoading.set(false);
      }
    });
  }

  onPageChange(newPage: number): void {
    this.loadQualifications(newPage);
  }

  verifyQualification(qualificationId: string): void {
    if (!confirm('Deseja marcar esta qualificação como VERIFICADA?')) return;

    this.qualificationService.verifyQualification(qualificationId).subscribe({
      next: () => {
        alert('Qualificação verificada com sucesso!');
        this.loadQualifications(this.currentPage());
      },
      error: () => alert('Erro ao verificar a qualificação.')
    });
  }

  deleteQualification(qualificationId: string): void {
    if (!confirm('Tem certeza de que deseja EXCLUIR esta qualificação? Esta ação não pode ser desfeita.')) return;

    this.qualificationService.deleteQualification(qualificationId).subscribe({
      next: () => {
        alert('Qualificação excluída com sucesso!');
        this.loadQualifications(this.currentPage());
      },
      error: () => alert('Erro ao excluir a qualificação.')
    });
  }

  getBadgeColor(level: string): string {
    switch (level?.toUpperCase()) {
      case 'JR':
      case 'JUNIOR':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'PL':
      case 'MID':
      case 'MID_LEVEL':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'SR':
      case 'SENIOR':
        return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
      default:
        return 'bg-gray-500/10 text-gray-400 border-gray-500/30';
    }
  }
}
